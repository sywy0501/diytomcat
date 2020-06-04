package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.catalina.Host;
import com.cs.tomcat.catalina.Engine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 获取解析xml工具类
 * @author: chushi
 * @create: 2020-06-03 17:03
 **/
public class ServerXMLUtil {

    public static List<Context> getContexts(){
        List<Context> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document d = Jsoup.parse(xml);

        Elements es = d.select("Context");
        for (Element e:es){
            String path = e.attr("path");
            String docBase = e.attr("docBase");
            Context context = new Context(path,docBase);
            result.add(context);
        }
        return result;
    }

    public static String getHostName (){
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document dc = Jsoup.parse(xml);

        Element host = dc.select("Host").first();
        return host.attr("name");
    }

    public static String getEngineDefaultHost(){
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);
        Element engine = document.select("Engine").first();
        return engine.attr("defaultHost");
    }

    public static List<Host> getHosts(Engine engine){
        List<Host> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);
        Elements elements = document.select("Host");
        for (Element element:elements){
            String name = element.attr("name");
            Host host = new Host(name,engine);
            result.add(host);
        }
        return result;
    }

    public static String getServiceName(){
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);
        Element service = document.select("Service").first();
        return service.attr("name");
    }

    public static String getServerName(){
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);
        Element server = document.select("server").first();
        return server.attr("name");
    }
}
