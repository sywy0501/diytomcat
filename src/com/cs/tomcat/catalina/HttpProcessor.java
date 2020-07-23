package com.cs.tomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.http.DefaultServlet;
import com.cs.tomcat.http.InvokerServlet;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.WebXMLUtil;
import com.cs.tomcat.webappservlet.HelloServlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
            System.out.println("uri: " + uri);
            Context context = request.getContext();
            String servletClassName = context.getServletClassName(uri);

            if (null != servletClassName) {
                InvokerServlet.getInstance().service(request, response);
            } else {
                DefaultServlet.getInstance().service(request, response);
            }

            if (Constant.CODE_200 == response.getStatus()) {
                handle200(s, response);
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

    private static void handle200(Socket s, Response response) throws IOException {
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
}
