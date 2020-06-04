package com.cs.tomcat;

import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import com.cs.tomcat.catalina.*;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.ServerXMLUtil;
import com.cs.tomcat.util.ThreadPoolUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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
