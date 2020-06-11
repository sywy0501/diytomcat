package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @description: 获取web.xml配置信息
 * @author: chushi
 * @create: 2020-06-10 17:49
 **/
public class ContextXMLUtil {

    public static String getWatchedResource(){
        try {
            String xml = FileUtil.readUtf8String(Constant.contextXmlFile);
            Document document = Jsoup.parse(xml);
            Element e = document.select("WatchedResource").first();
            return e.text();
        }catch (Exception e){
            e.printStackTrace();
            return "WEB-INF/web.xml";
        }
    }
}
