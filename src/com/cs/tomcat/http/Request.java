package com.cs.tomcat.http;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.catalina.Engine;
import com.cs.tomcat.catalina.Service;
import com.cs.tomcat.util.MiniBrowser;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: cs
 * @date: 2020/05/30 12:24
 * @desc:
 */
public class Request extends BaseRequest {

    private String requestString;
    private String uri;
    private Socket socket;
    private Context context;
    private Service service;
    //请求方式
    private String method;
    private String queryString;
    private Map<String,String[]> parameterMap;

    public Request(Socket socket,Service service) throws IOException {
        this.socket = socket;
        this.service = service;
        this.parameterMap = new HashMap<>();
        parseHttpRequest();
        if (StrUtil.isEmpty(requestString)) {
            return;
        }
        parseUri();
        //如果当前context路径不是"/"，对uri进行修正
        parseContext();
        parseMethod();
        if (!"/".equals(context.getPath())) {
            uri = StrUtil.removePrefix(uri, context.getPath());
            if (StrUtil.isEmpty(uri)){
                uri = "/";
            }
        }
        parseParameters();
    }

    private void parseHttpRequest() throws IOException {
        InputStream is = this.socket.getInputStream();
        //由于浏览器使用长链接，链接不会主动关闭 如果fully设置true 读取到不够bufferSize就不继续读取，就会卡住
        byte[] bytes = MiniBrowser.readBytes(is,false);
        requestString = new String(bytes, StandardCharsets.UTF_8);
    }

    private void parseParameters(){
        LogFactory.get().info(requestString);
        if ("GET".equals(this.getMethod())){
            String url = StrUtil.subBetween(requestString," "," ");
            if (StrUtil.contains(url,'?')){
                queryString = StrUtil.subAfter(url,'?',false);
            }
        }
        if ("POST".equals(this.getMethod())){
            queryString = StrUtil.subAfter(requestString,"\r\n\r\n",false);
        }
        if (null==queryString||0==queryString.length()){
            return;
        }
        queryString = URLUtil.decode(queryString);
        String[] parameterValues = queryString.split("&");
        if (null!=parameterValues){
            for (String parameterValue:parameterValues){
                String[] nameValues = parameterValue.split("=");
                String name =nameValues[0];
                String value = nameValues[1];
                String values[] = parameterMap.get(name);
                if (null == values){
                    values = new String[]{value};
                    parameterMap.put(name,values);
                }else {
                    values = ArrayUtil.append(values,value);
                    parameterMap.put(name,values);
                }
            }
        }
    }

    public ServletContext getServletContext(){
        return context.getServletContext();
    }

    public String getRealPath(String path){
        return context.getServletContext().getRealPath(path);
    }

    private void parseUri() {
        String temp;
        temp = StrUtil.subBetween(requestString, " ", " ");
        if (!StrUtil.contains(temp, '?')) {
            uri = temp;
            return;
        }
        temp = StrUtil.subBefore(temp, '?', false);
        uri = temp;
    }

    public String getUri() {
        return uri;
    }

    public String getRequestString() {
        return requestString;
    }

    public Context getContext() {
        return context;
    }

    /**
     * 增加解析Context的方法，通过获取uri中的信息来得到path，然后根据这个path来获取Context对象。如果获取不到 就获取对应的Root Context
     */
    private void parseContext() {
        Engine engine = service.getEngine();
        context = engine.getDefaultHost().getContext(uri);
        if (null!=context){
            return;
        }
        String path = StrUtil.subBetween(uri, "/", "/");
        if (null == path) {
            path = "/";
        } else {
            path = "/" + path;
        }
        context = engine.getDefaultHost().getContext(path);
        if (null == context) {
            context = engine.getDefaultHost().getContext("/");
        }
    }

    private void parseMethod(){
        method = StrUtil.subBefore(requestString," ",false);
    }

    @Override
    public String getMethod(){
        return method;
    }

    public String getParameter(String name){
        String[] values=parameterMap.get(name);
        if (null!=values&&0!=values.length){
            return values[0];
        }
        return null;
    }

    public Map getParameterMap(){
        return parameterMap;
    }

    public Enumeration getParameterNames(){
        return Collections.enumeration(parameterMap.keySet());
    }

    public String[] getParameterValues(String name){
        return parameterMap.get(name);
    }
}
