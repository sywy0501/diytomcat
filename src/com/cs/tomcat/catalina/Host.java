package com.cs.tomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.ServerXMLUtil;
import com.cs.tomcat.wathcer.WarFileWatcher;

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
        scanWarOnWebAppsFolder();
        new WarFileWatcher(this).start();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void scanContextInServerXML() {
        List<Context> contexts = ServerXMLUtil.getContexts(this);
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
        Context context = new Context(path, docBase,this,true);
        contextMap.put(context.getPath(), context);
    }

    public Context getContext(String path) {
        return contextMap.get(path);
    }

    public void reload(Context context){
        LogFactory.get().info("Reloading Context with name [{}] has started",context.getPath());
        //保存path docBase reloadable等信息
        String path = context.getPath();
        String docBase = context.getDocBase();
        boolean reloadable = context.isReloadable();
        //暂停
        context.stop();
        //从contextMap里删掉
        contextMap.remove(path);
        //创建新的context
        Context newContext = new Context(path,docBase,this,reloadable);
        //设置到contextMap中
        contextMap.put(newContext.getPath(),newContext);
        LogFactory.get().info("Reloading Context with name [{}] has completed",context.getPath());
    }

    public void load(File folder){
        String path = folder.getName();
        if ("ROOT".equals(path)){
            path="/";
        }else {
            path="/"+path;
        }
        String docBase = folder.getAbsolutePath();
        Context context = new Context(path,docBase,this,false);
        contextMap.put(context.getPath(),context);
    }

    public void loadWar(File warFile){
        String fileName = warFile.getName();
        String folderName = StrUtil.subBefore(fileName,".",true);
        //看看是否已经有对应的context
        Context context = getContext("/"+folderName);
        if (null!=context){
            return;
        }
        //先看是否已经有对应的文件夹
        File folder = new File(Constant.webappsFolder,folderName);
        if (folder.exists()){
            return;
        }
        //移动war文件，因为jar命令只支持解压到当前目录下
        File tempWarFile = FileUtil.file(Constant.webappsFolder,folderName,fileName);
        File contextFolder = tempWarFile.getParentFile();
        contextFolder.mkdirs();
        FileUtil.copyFile(warFile,tempWarFile);
        //解压
        String command = "jar xvf"+fileName;
        System.out.println(command);
        Process p = RuntimeUtil.exec(null,contextFolder,command);
        try {
            p.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }
        //解压之后删除临时war
        tempWarFile.delete();
        //然后创建新的Context
        load(contextFolder);
    }

    private void scanWarOnWebAppsFolder(){
        File folder = FileUtil.file(Constant.webappsFolder);
        File[] files = folder.listFiles();
        for (File file:files){
            if (!file.getName().toLowerCase().endsWith(".war")){
                continue;
            }
            loadWar(file);
        }
    }
}
