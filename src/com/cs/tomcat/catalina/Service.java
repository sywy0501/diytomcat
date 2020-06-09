package com.cs.tomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.util.ServerXMLUtil;

import java.util.List;

/**
 * @description:
 * @author: chushi
 * @create: 2020-06-04 14:38
 **/
public class Service {
    private String name;
    private Engine engine;
    private Server server;
    private List<Connector> connectors;

    public Service(Server server) {
        this.connectors = ServerXMLUtil.getConnectors(this);
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine(this);
        this.server = server;
    }

    public Engine getEngine() {
        return engine;
    }

    public Server getServer() {
        return server;
    }

    public void start() {
        init();
    }

    private void init() {
        TimeInterval timeInterval = DateUtil.timer();
        for (Connector c : connectors) {
            c.init();
        }
        LogFactory.get().info("Initialization processed in {} ms", timeInterval.intervalMs());
        for (Connector connector : connectors) {
            connector.start();
        }
    }
}
