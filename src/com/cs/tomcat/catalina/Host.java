package com.cs.tomcat.catalina;

import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.ServerXMLUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: chushi
 * @create: 2020-06-03 17:38
 **/
public class Host {
    private String name;
    private Engine engine;
    private Map<String, Context> contextMap;

    public Host(String name ,Engine engine) {
        this.contextMap = new HashMap<>();
        this.name = name;
        this.engine = engine;
        scanContextOnWebAppsFolder();
        scanContextInServerXML();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void scanContextInServerXML() {
        List<Context> contexts = ServerXMLUtil.getContexts();
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    private void scanContextOnWebAppsFolder() {
        File[] folders = Constant.webappsFolder.listFiles();
        if (null != folders) {
            for (File folder : folders) {
                if (!folder.isDirectory()) {
                    continue;
                }
                loadContext(folder);
            }
        }

    }

    private void loadContext(File folder) {
        String path = folder.getName();
        if ("ROOT".equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }
        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase);
        contextMap.put(context.getPath(), context);
    }

    public Context getContext(String path) {
        return contextMap.get(path);
    }
}
