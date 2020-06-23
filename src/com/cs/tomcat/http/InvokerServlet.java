package com.cs.tomcat.http;

import cn.hutool.core.util.ReflectUtil;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.util.Constant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description: 专门用于处理servlet
 * @author: chushi
 * @create: 2020-06-11 14:19
 **/
public class InvokerServlet extends HttpServlet {
    private static InvokerServlet instance = new InvokerServlet();
    public static synchronized InvokerServlet getInstance(){
        return instance;
    }

    private InvokerServlet(){}

    /**
     * @author: ChuShi
     * @date: 2020/6/11 2:37 下午
     * @param httpServletRequest
     * @param httpServletResponse
     * @return: void
     * @desc: 提供service方法，根据请求的uri获取servletClassName，然后实例化
     */
    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)throws IOException , ServletException {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;

        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);

        try {
            Class servletClass = context.getWebappClassLoader().loadClass(servletClassName);
            System.out.println("servletClass:"+servletClass);
            System.out.println("servletClass'classLoader:"+servletClass.getClassLoader());
            Object servletObject = context.getServlet(servletClass);
            ReflectUtil.invoke(servletObject,"service",request,response);
            response.setStatus(Constant.CODE_200);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
