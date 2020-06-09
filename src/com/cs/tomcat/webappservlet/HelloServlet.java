package com.cs.tomcat.webappservlet;

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
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        try {
            response.getWriter().println("Hello DIY Tomcat from HelloServlet");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
