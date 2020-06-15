package com.cs.tomcat.wathcer;

import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.catalina.Context;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @description:
 * @author: chushi
 * @create: 2020-06-15 17:46
 **/
public class ContextFileChangeWatcher {

    private WatchMonitor monitor;

    private boolean stop = false;

    public ContextFileChangeWatcher(Context context){
        this.monitor = WatchUtil.createAll(context.getDocBase(), Integer.MAX_VALUE, new Watcher() {

            private void dealWith(WatchEvent<?> event){
                synchronized (ContextFileChangeWatcher.class){
                    String fileName = event.context().toString();
                    if (stop){
                        return;
                    }
                    if (fileName.endsWith(".jar")||fileName.endsWith(".class")||fileName.endsWith(".xml")){
                        stop = true;
                        LogFactory.get().info(ContextFileChangeWatcher.this+"检测到了web应用下的重要文件变化{}",fileName);
                        context.reload();
                    }
                }
            }

            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }
        });
        this.monitor.setDaemon(true);
    }

    public void start(){
        monitor.start();
    }

    public void stop(){
        monitor.close();
    }
}
