package com.cs.tomcat;

import cn.hutool.core.util.NetUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * @description:
 * @author: chushi
 * @create: 2020-05-28 18:31
 **/
public class Bootstrap {

    public static void main(String[] args) {
        try{
            int port = 18080;

            //判断端口占用情况
            if (!NetUtil.isUsableLocalPort(port)){
                System.out.println(port+"端口已经被占用了，排查并关闭本端口");
                return;
            }

            //服务器和浏览器通过socket通信
            ServerSocket ss = new ServerSocket(port);

            while (true){
                //收到浏览器客户端的请求
                Socket s = ss.accept();
                //打开输入流准备接受浏览器提交信息
                InputStream is = s.getInputStream();
                //将浏览器信息读取并放入字节数组
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                is.read(buffer);
                //将字节数组转化成字符串并打印
                String requestString = new String(buffer,"UTF-8");
                System.out.println("浏览器输入信息：\r\n"+requestString);
                //打开输出流向客户端输出
                OutputStream os = s.getOutputStream();
                //准备发送的数据
                String responseHead = "HTTP/1.1 200 OK \r\n" + "Content-Type:text/html \r\n\r\n";
                String responseString = "<div style='color:blue' >Hello DIY Tomcat!</div>";
                responseString = responseHead + responseString;
                //将字符串转换成字节数组发出去
                os.write(responseString.getBytes());
                os.flush();
                //关闭对应的socket
                s.close();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
