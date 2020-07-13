package com.cs.tomcat.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @description:
 * @author: chushi
 * @create: 2020-06-23 18:17
 **/
public class StandardServletConfig implements ServletConfig {

    private ServletContext servletContext;
    private Map<String, String> initParameters;
    private String servletName;

    public StandardServletConfig(ServletContext servletContext,String servletName,Map<String,String> initParameters){
        this.servletContext = servletContext;
        this.initParameters = initParameters;
        this.servletName = servletName;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return initParameters.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }
}
