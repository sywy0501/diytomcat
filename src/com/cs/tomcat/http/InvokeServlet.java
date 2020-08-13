package com.cs.tomcat.http;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.LogFactory;
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
public class InvokeServlet extends HttpServlet {
    private static InvokeServlet instance = new InvokeServlet();
    public static synchronized InvokeServlet getInstance(){
        return instance;
    }

    private InvokeServlet(){}

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
            LogFactory.get().info("servletClass:"+servletClass);
            LogFactory.get().info("servletClass'classLoader:"+servletClass.getClassLoader());
            Object servletObject = context.getServlet(servletClass);
            ReflectUtil.invoke(servletObject,"service",request,response);

            if (null!=response.getRedirectPath()){
                response.setStatus(Constant.CODE_302);
            }else {
                response.setStatus(Constant.CODE_200);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
