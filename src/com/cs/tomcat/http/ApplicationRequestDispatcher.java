package com.cs.tomcat.http;

import com.cs.tomcat.catalina.HttpProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @description:
 * @author: chushi
 * @create: 2020-08-13 14:24
 **/
public class ApplicationRequestDispatcher implements RequestDispatcher {

    private String uri;

    public ApplicationRequestDispatcher(String uri){
        if (!uri.startsWith("/")){
            uri = "/"+uri;
        }
        this.uri = uri;
    }

    /**
     * @author: ChuShi
     * @date: 2020/8/13 2:38 下午
     * @param servletRequest
     * @param servletResponse
     * @return: void
     * @desc: 修改request的uri，然后通过httpProcessor的execute再执行一次，相当于服务器内部再次访问了某个页面
     */
    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        Request request = (Request) servletRequest;
        Response response = (Response) servletResponse;

        request.setUri(uri);

        HttpProcessor processor = new HttpProcessor();
        processor.execute(request.getSocket(),request,response);
        request.setForwarded(true);
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {

    }
}
