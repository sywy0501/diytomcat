package com.cs.tomcat;

import com.cs.tomcat.catalina.*;
import com.cs.tomcat.classloader.CommonClassLoader;

import java.lang.reflect.Method;

/**
 * @description:
 * @author: chushi
 * @create: 2020-05-28 18:31
 **/
public class Bootstrap {

    public static void main(String[] args)throws Exception {
        /*Server server = new Server();
        server.start();*/
        CommonClassLoader commonClassLoader = new CommonClassLoader();
        Thread.currentThread().setContextClassLoader(commonClassLoader);
        String serverClassName = "com.cs.tomcat.catalina.Server";
        Class<?> serverClazz = commonClassLoader.loadClass(serverClassName);
        Object serverObject = serverClazz.newInstance();
        Method m = serverClazz.getMethod("start");
        m.invoke(serverObject);
        System.out.println(serverClazz.getClassLoader());
    }
}
