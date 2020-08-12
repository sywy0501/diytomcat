package com.cs.tomcat.catalina;

import cn.hutool.log.LogFactory;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.ThreadPoolUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @description: 端口映射类
 * @author: chushi
 * @create: 2020-06-08 17:36
 **/
public class Connector implements Runnable{

    int port;
    private Service service;

    private String compression;
    private int compressionMinSize;
    private String noCompressionUserAgents;
    private String compressableMimeType;

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public int getCompressionMinSize() {
        return compressionMinSize;
    }

    public void setCompressionMinSize(int compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }

    public String getNoCompressionUserAgents() {
        return noCompressionUserAgents;
    }

    public void setNoCompressionUserAgents(String noCompressionUserAgents) {
        this.noCompressionUserAgents = noCompressionUserAgents;
    }

    public String getCompressableMimeType() {
        return compressableMimeType;
    }

    public void setCompressableMimeType(String compressableMimeType) {
        this.compressableMimeType = compressableMimeType;
    }

    public Connector (Service service){
        this.service = service;
    }

    public Service getService(){
        return service;
    }

    public void setPort(int port){
        this.port = port;
    }

    @Override
    public void run() {
        try {
            //服务器和浏览器通过socket通信
            ServerSocket ss = new ServerSocket(port);

            while (true) {
                //收到浏览器客户端的请求
                Socket s = ss.accept();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(s,Connector.this);
                            Response response = new Response();
                            HttpProcessor httpProcessor = new HttpProcessor();
                            httpProcessor.execute(s,request,response);
                        } catch (IOException e) {
                            LogFactory.get().error(e);
                        } finally {
                            try {
                                if (!s.isClosed()) {
                                    s.close();
                                }
                            } catch (Exception e) {
                                LogFactory.get().error(e);
                            }
                        }
                    }
                };
                ThreadPoolUtil.run(runnable);
            }

        } catch (Exception e) {
            LogFactory.get().error(e);
        }
    }

    public void init() {
        LogFactory.get().info("Initializing ProtocolHandler [http-bio-{}]",port);
    }

    public void start() {
        LogFactory.get().info("Starting ProtocolHandler [http-bio-{}]",port);
        new Thread(this).start();
    }
}
