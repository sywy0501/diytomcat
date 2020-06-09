package com.cs.tomcat;

import com.cs.tomcat.catalina.*;

/**
 * @description:
 * @author: chushi
 * @create: 2020-05-28 18:31
 **/
public class Bootstrap {

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
