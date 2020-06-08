package com.cs.tomcat.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @author: cs
 * @date: 2020/05/30 16:10
 * @desc:
 */
public class Response {

    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private String contentType;
    private byte[] body;

    public void setBody(byte[] body){
        this.body = body;
    }

    public byte[] getBody()throws UnsupportedEncodingException{
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

    public String getContentType(){
        return contentType;
    }

    public PrintWriter getPrintWriter(){
        return printWriter;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }
}
