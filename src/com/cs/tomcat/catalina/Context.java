package com.cs.tomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.exception.WebConfigDuplicatedException;
import com.cs.tomcat.util.ContextXMLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.*;

/**
 * @description: 应用类
 * @author: chushi
 * @create: 2020-06-03 16:00
 **/
public class Context {
    private String path;
    private String docBase;
    //对应WEB-INF/web.xml文件
    private File contextWebXmlFile;
    //地址对应servlet的类名
    private Map<String, String> url_servletClassName;
    //地址对应servlet的名称
    private Map<String,String> url_servletName;
    //servlet的名称对应类名
    private Map<String,String> servletName_className;
    //service类名对应名称
    private Map<String,String> className_servletName;

    public Context(String path,String docBase){
        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());
        this.url_servletClassName = new HashMap<>();
        this.url_servletName = new HashMap<>();
        this.servletName_className = new HashMap<>();
        this.className_servletName = new HashMap<>();
        deploy();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    /**
     * @author: ChuShi
     * @date: 2020/6/11 10:42 上午
     * @param d
     * @return: void
     * @desc: 将对应信息从web.xml中解析出来
     */
    private void parseServletMapping(Document d){
        Elements mappingurlElements = d.select("servlet-mapping url-pattern");
        for(Element mappingurlElement:mappingurlElements){
            String urlPattern = mappingurlElement.text();
            String servletName = mappingurlElement.parent().select("servlet-name").first().text();
            url_servletName.put(urlPattern,servletName);
        }
        Elements servletNameEmements = d.select("servlet servlet-name");
        for (Element servletNameElement:servletNameEmements){
            String servletName = servletNameElement.text();
            String servletClass = servletNameElement.parent().select("servlet-class").first().text();
            servletName_className.put(servletName,servletClass);
            className_servletName.put(servletClass,servletName);
        }
        Set<String> urls = url_servletName.keySet();
        for(String url:urls){
            String servletName=url_servletName.get(url);
            String servletClassName = servletName_className.get(servletName);
            url_servletClassName.put(url,servletClassName);
        }
    }

    private void checkDuplicate(Document d,String mapping,String desc)throws WebConfigDuplicatedException{
        Elements elements = d.select(mapping);
        //判断逻辑释放入一个集合，然后把集合排序后看相邻两个元素是否相同
        List<String> contents = new ArrayList<>();
        for (Element e:elements){
            contents.add(e.text());
        }
        Collections.sort(contents);
        for (int i=0;i<contents.size()-1;i++){
            String contentPre = contents.get(i);
            String contentNext = contents.get(i+1);
            if (contentPre.equals(contentNext)){
                throw new WebConfigDuplicatedException(StrUtil.format(desc,contentPre));
            }
        }
    }

    private void checkDuplicate()throws WebConfigDuplicatedException{
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        checkDuplicate(d,"servlet-mapping url-pattern","servlet url重复，请保持其唯一性:{}");
        checkDuplicate(d,"servlet servlet-name","servlet 名称重复，请保持其唯一性:{}");
        checkDuplicate(d,"servlet servlet-class","servlet 类名重复，请保持其唯一性:{}");
    }

    /**
     * @author: ChuShi
     * @date: 2020/6/11 11:09 上午
     * @param
     * @return: void
     * @desc: 初始化
     */
    private void init(){
        //判断是否有web.xml文件
        if (!contextWebXmlFile.exists()){
            return;
        }
        try {
            //判断是否重复
            checkDuplicate();
        }catch (WebConfigDuplicatedException e){
            e.printStackTrace();
            return;
        }
        //进行web.xml的解析
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        parseServletMapping(d);
    }

    private void deploy(){
        TimeInterval timeInterval = DateUtil.timer();
        LogFactory.get().info("Deploying web application directory:{}",this.docBase);
        init();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms",this.getDocBase(),timeInterval.intervalMs());
    }

    public String getServletClassName(String uri){
        return url_servletClassName.get(uri);
    }
}
