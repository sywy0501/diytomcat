package com.cs.tomcat.http;

import com.cs.tomcat.catalina.Context;

import java.io.File;
import java.util.*;

/**
 * @description: servlet相关属性
 * @author: chushi
 * @create: 2020-06-23 17:35
 **/
public class ApplicationContext extends BaseServletContext{

    private Map<String, Object> attributesMap;
    private Context context;

    public ApplicationContext(Context context){
        this.attributesMap = new HashMap<>();
        this.context = context;
    }

    public void removeAttribute(String name){
        attributesMap.remove(name);
    }

    public void setAttributesMap(String name,Object value){
        attributesMap.put(name,value);
    }

    public Map<String, Object> getAttributesMap(String name){
        return attributesMap;
    }

    public Enumeration<String> getAttributeNames(){
        Set<String> keys = attributesMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public String getRealPath(String path){
        return new File(context.getDocBase(),path).getAbsolutePath();
    }
}
