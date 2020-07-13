package com.cs.tomcat.webappservlet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description: servlet初体验
 * @author: chushi
 * @create: 2020-06-09 15:28
 **/
public class HelloServlet extends HttpServlet {

    public HelloServlet(){
        System.out.println(this+"的构造方法");
    }

    @Override
    public void init(ServletConfig config){
        String author = config.getInitParameter("author");
        String site = config.getInitParameter("site");

        System.out.println(this + "的初始化方法init");
        System.out.println("获取到了参数author："+author);
        System.out.println("获取到了参数site："+site);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        try {
            System.out.println(this+"的doGet()方法 init");
            response.getWriter().println("Hello DIY Tomcat from HelloServlet");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void destroy(){
        System.out.println(this+"被销毁");
    }
}
