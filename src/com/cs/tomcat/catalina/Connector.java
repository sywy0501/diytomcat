package com.cs.tomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.ThreadPoolUtil;
import com.cs.tomcat.util.WebXMLUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @description: 端口映射类
 * @author: chushi
 * @create: 2020-06-08 17:36
 **/
public class Connector implements Runnable{

    int port;
    private Service service;

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
                            Request request = new Request(s,service);
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
