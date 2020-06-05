package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import com.cs.tomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

/**
 * @description: 获取context下的欢迎文件
 * @author: chushi
 * @create: 2020-06-05 14:36
 **/
public class WebXMLUtil {
    public static String getWelcomeFile(Context context){
        String xml = FileUtil.readUtf8String(Constant.webXmlFile);
        Document d = Jsoup.parse(xml);
        Elements elements = d.select("welcome-file");
        for (Element element:elements){
            String welcomeFileName = element.text();
            File f = new File(context.getDocBase(),welcomeFileName);
            if (f.exists()){
                return f.getName();
            }
        }
        return "index.html";
    }
}
