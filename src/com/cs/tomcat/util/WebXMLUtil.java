package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import com.cs.tomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.cs.tomcat.util.Constant.webXmlFile;

/**
 * @description: 获取context下的欢迎文件
 * @author: chushi
 * @create: 2020-06-05 14:36
 **/
public class WebXMLUtil {

    private static Map<String, String> mimeTypeMapping = new HashMap<>();

    public static String getWelcomeFile(Context context){
        String xml = FileUtil.readUtf8String(webXmlFile);
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

    private static void initMimeType(){
        String xml = FileUtil.readUtf8String(webXmlFile);
        Document document = Jsoup.parse(xml);
        Elements es = document.select("mime-mapping");
        for (Element element:es){
            String extName = element.select("extension").first().text();
            String mimeType = element.select("mime-type").first().text();
            mimeTypeMapping.put(extName,mimeType);
        }
    }

    public static synchronized String getMimeType(String extName){
        if (mimeTypeMapping.isEmpty()){
            initMimeType();
        }
        String mimeType = mimeTypeMapping.get(extName);
        if (null==mimeType){
            return "text/html";
        }
        return mimeType;
    }
}
