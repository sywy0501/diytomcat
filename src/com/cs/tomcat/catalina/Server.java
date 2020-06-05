package com.cs.tomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.ServerXMLUtil;
import com.cs.tomcat.util.ThreadPoolUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @author: chushi
 * @create: 2020-06-04 14:52
 **/
public class Server {
    private Service service;

    public Server() {
        this.service = new Service(this);
    }

    public void start() {
        logJVM();
        init();
    }

    private static void logJVM() {
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Server version", "How2j DiyTomcat/1.0.1");
        infos.put("Server built", "2020-04-08 10:20:22");
        infos.put("Server number", "1.0.1");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("java.home"));
        infos.put("Java Home", SystemUtil.get("java home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));
        Set<String> keys = infos.keySet();
        for (String key : keys) {
            LogFactory.get().info(key + ":\t\t" + infos.get(key));
        }
    }

    private void init() {
        try {
            int port = 18080;

            //服务器和浏览器通过socket通信
            ServerSocket ss = new ServerSocket(port);

            while (true) {
                //收到浏览器客户端的请求
                Socket s = ss.accept();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //打开输入流准备接受浏览器提交信息
                            Request request = new Request(s, service);
                            //将浏览器信息读取并放入字节数组
                            //将字节数组转化成字符串并打印
                            //System.out.println("浏览器输入信息：\r\n" + request.getRequestString());
                            //System.out.println("uri:" + request.getUri());

                            Response response = new Response();

                            String uri = request.getUri();
                            if (null == uri) {
                                return;
                            }
                            System.out.println("uri: " + uri);
                            Context context = request.getContext();
                            //如果是"/"就返回原字符串
                            if ("/".equals(uri)) {
                                String html = "Hello DIY Tomcat ";
                                response.getPrintWriter().println(html);
                            } else {
                                //获取文件名
                                String fileName = StrUtil.removePrefix(uri, "/");
                                //获取文件对象
                                File file = FileUtil.file(context.getDocBase(), fileName);
                                //文件存在则打印，不存在返回相关信息
                                if (file.exists()) {
                                    String fileContent = FileUtil.readUtf8String(file);
                                    response.getPrintWriter().println(fileContent);

                                    if (fileName.equals("timeConsume.html")) {
                                        ThreadUtil.sleep(1000);
                                    }
                                } else {
                                    handle404(s, uri);
                                }
                            }

                            handle200(s, response);
                        } catch (Exception e) {
                            e.printStackTrace();
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
                };
                ThreadPoolUtil.run(runnable);
            }

        } catch (Exception e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }

    private static void handle200(Socket s, Response response) throws IOException {
        //准备发送的数据
        //根据response对象上的contentType，组成返回的头信息，并转换成字节数组
        String contentType = response.getContentType();
        String headText = Constant.RESPONSE_HEAD_202;
        headText = StrUtil.format(headText, contentType);

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
}
