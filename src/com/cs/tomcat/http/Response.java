package com.cs.tomcat.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.log.LogFactory;

import javax.servlet.http.Cookie;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author: cs
 * @date: 2020/05/30 16:10
 * @desc:
 */
public class Response extends BaseResponse {

    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private String contentType;
    private byte[] body;
    private int status;
    private List<Cookie> cookies;

    @Override
    public void setStatus(int status){
        this.status = status;
    }

    @Override
    public int getStatus(){
        return status;
    }

    public void setBody(byte[] body){
        this.body = body;
    }

    public byte[] getBody(){
        if (null==body){
            String content = stringWriter.toString();
            body = content.getBytes(StandardCharsets.UTF_8);
        }
        return body;
    }

    public Response(){
        this.stringWriter = new StringWriter();
        this.contentType = "text/html";
        this.printWriter = new PrintWriter(stringWriter);
        this.cookies = new ArrayList<>();
    }

    @Override
    public PrintWriter getWriter(){
        return printWriter;
    }

    @Override
    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    @Override
    public String getContentType(){
        return contentType;
    }

    @Override
    public void addCookie(Cookie cookie){
        cookies.add(cookie);
    }

    public List<Cookie> getCookies(){
        return this.cookies;
    }

    public String getCookiesHeader(){
        if (null==cookies){
            return "";
        }
        String pattern = "EEE,d MMM yyyy HH:mm:ss 'GMT'";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        StringBuilder sb = new StringBuilder();
        for (Cookie cookie:getCookies()){
            sb.append("\r\n");
            sb.append("Set-Cookie:");
            LogFactory.get().info(cookie.getName()+"="+cookie.getValue()+";");
            sb.append(cookie.getName()+"="+cookie.getValue()+";");
            if (-1!=cookie.getMaxAge()){
                sb.append("Expires=");
                Date now = new Date();
                Date expire = DateUtil.offset(now, DateField.MINUTE,cookie.getMaxAge());
                sb.append(sdf.format(expire));
                sb.append(",");
            }
            if (null!=cookie.getPath()){
                sb.append("Path="+cookie.getPath());
            }
        }
        return sb.toString();
    }
}
