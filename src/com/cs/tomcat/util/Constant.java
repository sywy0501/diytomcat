package com.cs.tomcat.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

/**
 * @author: cs
 * @date: 2020/05/30 16:08
 * @desc:
 */
public class Constant {

    public final static String RESPONSE_HEAD_202 = "HTTP/1.1 200 OK\r\n"+"Content-Type:{}\r\n\r\n";

    public final static File webappsFolder = new File(SystemUtil.get("user.dir"),"webapps");
    public final static File rootFolder = new File(webappsFolder,"ROOT");

    public final static File confFolder = new File(SystemUtil.get("user.dir"),"conf");
    public final static File serverXmlFile = new File(confFolder,"server.xml");

}
