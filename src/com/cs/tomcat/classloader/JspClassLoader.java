package com.cs.tomcat.classloader;

import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.util.Constant;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 一个jsp对应一个JapClassLoader
 *               如果这个jsp文件修改了，就要换一个新的JspClassLoader
 *               JspClassLoader基于由jsp文件转移并编译出来的class文件，进行类的加载
 * @author: chushi
 * @create: 2020-08-13 09:38
 **/
public class JspClassLoader extends URLClassLoader {

    private static Map<String,JspClassLoader> map = new HashMap<>();

    public static void invalidJspClassLoader(String uri, Context context){
        String key = context.getPath()+"/"+uri;
        map.remove(key);
    }

    public static JspClassLoader getJspClassLoader(String uri,Context context){
        String key = context.getPath()+"/"+uri;
        JspClassLoader loader = map.get(key);
        if (null==loader){
            loader = new JspClassLoader(context);
            map.put(key,loader);
        }
        return loader;
    }

    private JspClassLoader(Context context){
        super(new URL[]{},context.getWebappClassLoader());

        try {
            String subFolder;
            String path = context.getPath();
            if ("/".equals(path)){
                subFolder="_";
            }else {
                subFolder= StrUtil.subAfter(path,'/',false);
            }

            File classesFolder = new File(Constant.workFolder,subFolder);
            URL url = new URL("file:"+classesFolder.getAbsolutePath()+"/");
            this.addURL(url);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
