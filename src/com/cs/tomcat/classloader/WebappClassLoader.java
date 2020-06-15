package com.cs.tomcat.classloader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: chushi
 * @create: 2020-06-14 14:38
 **/
public class WebappClassLoader extends URLClassLoader {

    public WebappClassLoader(String docBase, ClassLoader commonClassLoader){
        super(new URL[]{},commonClassLoader);

        try {
            File webinfFolder = new File(docBase,"WEB-INF");
            File classFoler = new File(webinfFolder,"classes");
            File libFolder = new File(webinfFolder,"lib");
            URL url;
            url = new URL("file:"+classFoler.getAbsolutePath()+"/");
            this.addURL(url);
            List<File> jarFile = FileUtil.loopFiles(libFolder);
            for (File file:jarFile){
                url = new URL("file:"+file.getAbsolutePath());
                this.addURL(url);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop(){
        try {
            close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
