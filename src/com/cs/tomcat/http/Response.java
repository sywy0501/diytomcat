package com.cs.tomcat.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
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
    }

    public PrintWriter getWriter(){
        return printWriter;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    public String getContentType(){
        return contentType;
    }
}
