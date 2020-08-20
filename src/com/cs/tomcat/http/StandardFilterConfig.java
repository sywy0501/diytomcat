package com.cs.tomcat.http;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: chushi
 * @create: 2020-08-19 11:57
 **/
public class StandardFilterConfig implements FilterConfig {

    private ServletContext servletContext;
    private Map<String, String> initParameters;
    private String filterName;

    public StandardFilterConfig(ServletContext servletContext,String filterName,Map<String, String> initParameters){
        this.servletContext = servletContext;
        this.filterName = filterName;
        this.initParameters = initParameters;
        if (null==this.initParameters){
            this.initParameters = new HashMap<>();
        }
    }

    @Override
    public String getFilterName() {
        return filterName;
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
