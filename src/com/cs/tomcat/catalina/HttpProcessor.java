package com.cs.tomcat.catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.http.*;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.SessionManager;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @description: 处理请求
 * @author: chushi
 * @create: 2020-06-09 14:30
 **/
public class HttpProcessor {

    public void execute(Socket s, Request request, Response response) {
        try {
            //打开输入流准备接受浏览器提交信息
            String uri = request.getUri();
            if (null == uri) {
                return;
            }
            prepareSession(request, response);
            System.out.println("uri: " + uri);
            Context context = request.getContext();
            String servletClassName = context.getServletClassName(uri);
            HttpServlet workingServlet;

            if (null != servletClassName) {
                workingServlet = InvokeServlet.getInstance();
            }else  if (uri.endsWith(".jsp")){
                workingServlet = JspServlet.getInstance();
            } else {
                workingServlet = DefaultServlet.getInstance();
            }

            List<Filter> filters = request.getContext().getMatchedFilter(request.getRequestURI());
            ApplicationFilterChain filterChain = new ApplicationFilterChain(filters,workingServlet);
            filterChain.doFilter(request,response);
            if (request.isForwarded()){
                return;
            }
            if (Constant.CODE_200 == response.getStatus()) {
                handle200(s,request,response);
                return;
            }

            if (Constant.CODE_302==response.getStatus()){
                handle302(s,response);
                return;
            }

            if (Constant.CODE_404 == response.getStatus()) {
                handle404(s, uri);
                return;
            }
        } catch (Exception e) {
            LogFactory.get().error(e);
            handle500(s, e);
        } finally {
            try {
                if (!s.isClosed()) {
                    s.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*private static void handle200(Socket s, Response response) throws IOException {
        //准备发送的数据
        //根据response对象上的contentType，组成返回的头信息，并转换成字节数组
        String contentType = response.getContentType();
        String headText = Constant.RESPONSE_HEAD_202;
        String cookiesHeader = response.getCookiesHeader();
        headText = StrUtil.format(headText, contentType,cookiesHeader);

        byte[] head = headText.getBytes();
        //获取主题信息部分，即html对应的字节数组
        byte[] body = response.getBody();
        //拼接头信息和主题信息，成为一个响应字节数组
        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);

        //将字符串转换成字节数组发出去
        OutputStream os = s.getOutputStream();
        os.write(responseBytes);
    }*/

    /**
     * @author: ChuShi
     * @date: 2020/8/12 9:23 上午
     * @param s
     * @param request
     * @param response
     * @return: void
     * @desc: 需要进行gzip压缩的使用gzip投，把body用zipUtil进行zip压缩
     */
    private void handle200(Socket s,Request request,Response response)throws IOException{
        OutputStream os = s.getOutputStream();
        String contentType = response.getContentType();

        byte[] body = response.getBody();
        String cookiesHeader = response.getCookiesHeader();

        boolean gzip = isGzip(request,body,contentType);

        String headText;
        if (gzip){
            headText = Constant.response_head_200_gzip;
        }else {
            headText = Constant.response_head_200;
        }
        headText = StrUtil.format(headText,contentType,cookiesHeader);
        if (gzip){
            body = ZipUtil.gzip(body);
        }
        byte[] head = headText.getBytes();
        byte[] responseBytes = new byte[head.length+body.length];
        ArrayUtil.copy(head,0,responseBytes,0,head.length);
        ArrayUtil.copy(body,0,responseBytes,head.length,body.length);
        os.write(responseBytes,0,responseBytes.length);
        os.flush();
        os.close();
    }

    protected void handle404(Socket s, String uri) throws IOException {
        OutputStream os = s.getOutputStream();
        String responseText = StrUtil.format(Constant.textFormat_404, uri, uri);
        responseText = Constant.response_head_404 + responseText;
        byte[] responseByte = responseText.getBytes(StandardCharsets.UTF_8);
        os.write(responseByte);
    }

    protected void handle500(Socket s, Exception e) {
        try {
            OutputStream os = s.getOutputStream();
            StackTraceElement stes[] = e.getStackTrace();
            StringBuffer sb = new StringBuffer();
            sb.append(e.toString());
            sb.append("\r\n");

            for (StackTraceElement ste : stes) {
                sb.append("\t");
                sb.append(ste.toString());
                sb.append("\r\n");
            }
            String msg = e.getMessage();

            if (null != msg && msg.length() > 0) {
                msg = msg.substring(0, 19);
            }
            String text = StrUtil.format(Constant.textFormat_500, msg, e.toString(), sb.toString());
            text = Constant.response_head_500 + text;
            byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
            os.write(responseBytes);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void handle302(Socket s,Response response)throws IOException{
        OutputStream os = s.getOutputStream();
        String redirectPath = response.getRedirectPath();
        String head_text = Constant.response_head_302;
        String header = StrUtil.format(head_text,redirectPath);
        byte[] responseBytes = header.getBytes(StandardCharsets.UTF_8);
        os.write(responseBytes);
    }

    public void prepareSession(Request request,Response response){
        String jsessionid = request.getJSessionIdFromCookie();
        HttpSession session = SessionManager.getSession(jsessionid,request,response);
        request.setSession(session);
    }

    /**
     * @author: ChuShi
     * @date: 2020/8/12 9:24 上午
     * @param request
     * @param body
     * @param mimeType
     * @return: boolean
     * @desc: 判断是否要进行gzip压缩
     */
    private boolean isGzip(Request request,byte[] body,String mimeType){
        String acceptEncodings = request.getHeader("Accept-Encoding");
        if (!StrUtil.containsAny(acceptEncodings,"gzip")){
            return false;
        }

        Connector connector = request.getConnector();
        if (mimeType.contains(";")){
            mimeType=StrUtil.subBefore(mimeType,";",false);
        }
        if (!"on".equals(connector.getCompression())){
            return false;
        }
        if (body.length<connector.getCompressionMinSize()){
            return false;
        }
        String userAgents = connector.getNoCompressionUserAgents();
        String[] eachUserAgents = userAgents.split(",");
        for (String eachUserAgent:eachUserAgents){
            eachUserAgent = eachUserAgent.trim();
            String userAgent = request.getHeader("User-Agent");
            if (StrUtil.containsAny(userAgent,eachUserAgent)){
                return false;
            }
        }
        String mimeTypes = connector.getCompressableMimeType();
        String[] eachMineTypes = mimeTypes.split(",");
        for (String eachMineType:eachMineTypes){
            if (mimeType.equals(eachMineType)){
                return true;
            }
        }
        return false;
    }
}
