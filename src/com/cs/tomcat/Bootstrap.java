package com.cs.tomcat;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.Constant;

import java.io.IOException;
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
                Request request = new Request(s);
                //将浏览器信息读取并放入字节数组
                //将字节数组转化成字符串并打印
                System.out.println("浏览器输入信息：\r\n"+request.getRequestString());
                System.out.println("uri:"+request.getUri());

                Response response = new Response();
                String html = "Hello DIY Tomcat ";
                response.getPrintWriter().println(html);

                handle200(s,response);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void handle200(Socket s, Response response)throws IOException{
        //准备发送的数据
        String contentType = response.getContentType();
        String headText = Constant.RESPONSE_HEAD_202;
        headText = StrUtil.format(headText,contentType);

        byte[] head = headText.getBytes();

        byte[] body = response.getBody();

        byte[] responseBytes = new byte[head.length+body.length];
        ArrayUtil.copy(head,0,responseBytes,0,head.length);
        ArrayUtil.copy(body,0,responseBytes,head.length,body.length);

        OutputStream os = s.getOutputStream();
        //将字符串转换成字节数组发出去
        os.write(responseBytes);
        //关闭对应的socket
        s.close();
    }
}
