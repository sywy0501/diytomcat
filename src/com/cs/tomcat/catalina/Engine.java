package com.cs.tomcat.catalina;

import com.cs.tomcat.catalina.Host;
import com.cs.tomcat.util.ServerXMLUtil;

import java.util.List;

/**
 * @description: 引擎实体类
 * @author: chushi
 * @create: 2020-06-04 11:43
 **/
public class Engine {
    private String defaultHost;
    private List<Host> hosts;
    private Service service;

    public Engine(Service service){
        this.defaultHost = ServerXMLUtil.getEngineDefaultHost();
        this.hosts = ServerXMLUtil.getHosts(this);
        this.service = service;
        checkDefault();
    }

    private void checkDefault(){
        if (null==getDefaultHost()){
            throw new RuntimeException("the defaultHost"+defaultHost+"does not exist");
        }
    }

    public Host getDefaultHost(){
        for (Host host:hosts){
            if (host.getName().equals(defaultHost)){
                return host;
            }
        }
        return null;
    }
}
