package com.cs.tomcat.http;

import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.util.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author: cs
 * @date: 2020/05/30 12:24
 * @desc:
 */
public class Request {

    private String requestString;
    private String uri;
    private Socket socket;

    public Request(Socket socket)throws IOException{
        this.socket = socket;
        parseHttpRequest();
        if (StrUtil.isEmpty(requestString)){
            return;
        }
        parseUri();

    }

    private void parseHttpRequest()throws IOException{
        InputStream is = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(is);
        requestString = new String(bytes, StandardCharsets.UTF_8);
    }

    private void parseUri(){
        String temp;
        temp = StrUtil.subBetween(requestString," "," ");
        if (!StrUtil.contains(temp,'?')){
            uri = temp;
            return;
        }
        temp = StrUtil.subBefore(temp,'?',false);
        uri = temp;
    }

    public String getUri(){
        return uri;
    }

    public String getRequestString(){
        return requestString;
    }
}
