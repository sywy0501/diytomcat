package com.cs.tomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.classloader.WebappClassLoader;
import com.cs.tomcat.exception.WebConfigDuplicatedException;
import com.cs.tomcat.http.ApplicationContext;
import com.cs.tomcat.http.StandardFilterConfig;
import com.cs.tomcat.http.StandardServletConfig;
import com.cs.tomcat.util.ContextXMLUtil;
import com.cs.tomcat.wathcer.ContextFileChangeWatcher;
import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
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
    private Map<String, String> url_servletName;
    //servlet的名称对应类名
    private Map<String, String> servletName_className;
    //service类名对应名称
    private Map<String, String> className_servletName;

    private WebappClassLoader webappClassLoader;

    private Host host;

    private boolean reloadable;

    private ContextFileChangeWatcher contextFileChangeWatcher;

    private ServletContext servletContext;

    private Map<Class<?>, HttpServlet> servletPool;

    private Map<String, Map<String, String>> servletClassNameInitParams;

    private List<String> loadOnStartupServletClassNames;

    private Map<String,List<String>> urlFilterClassName;

    private Map<String,List<String>> urlFilterNames;

    private Map<String,String> filterNameClassName;

    private Map<String, String> classNameFilterName;

    private Map<String,Map<String, String>> filterClassNameInitParams;

    private Map<String, Filter> filterPool;

    public Context(String path, String docBase, Host host, boolean reloadable) {
        TimeInterval timeInterval = DateUtil.timer();
        this.host = host;
        this.reloadable = reloadable;
        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());
        this.url_servletClassName = new HashMap<>();
        this.url_servletName = new HashMap<>();
        this.servletName_className = new HashMap<>();
        this.className_servletName = new HashMap<>();
        this.servletContext = new ApplicationContext(this);
        this.servletPool = new HashMap<>();
        this.servletClassNameInitParams = new HashMap<>();
        this.loadOnStartupServletClassNames = new ArrayList<>();
        this.urlFilterClassName = new HashMap<>();
        this.urlFilterNames = new HashMap<>();
        this.filterNameClassName = new HashMap<>();
        this.classNameFilterName = new HashMap<>();
        this.filterClassNameInitParams = new HashMap<>();
        this.filterPool = new HashMap<>();

        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
        this.webappClassLoader = new WebappClassLoader(docBase, commonClassLoader);

        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        deploy();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase, timeInterval.intervalMs());
    }

    public synchronized HttpServlet getServlet(Class<?> clazz) throws IllegalAccessException, InstantiationException, ServletException {
        HttpServlet servlet = servletPool.get(clazz);
        if (null == servlet) {
            servlet = (HttpServlet) clazz.newInstance();
            ServletContext servletContext = this.getServletContext();

            String className = clazz.getName();
            String servletName = className_servletName.get(className);

            Map<String,String> initParameters = servletClassNameInitParams.get(className);
            ServletConfig servletConfig = new StandardServletConfig(servletContext,servletName,initParameters);

            servlet.init(servletConfig);
            servletPool.put(clazz, servlet);
        }
        return servlet;
    }

    /**
     * @desc: 解析需要做自启动的类
     */
    public void parseLoadOnStartup(Document d){
        Elements es = d.select("load-on-startup");
        for (Element e:es){
            String loadOnStartupServletClassName = e.parent().select("servlet-class").text();
            loadOnStartupServletClassNames.add(loadOnStartupServletClassName);
        }
    }

    /**
     * @desc: 对相关的类做自启动
     */
    public void handleLoadOnStartup(){
        for (String loadOnStartupServletClassName:loadOnStartupServletClassNames){
            try {
                Class<?> clazz = webappClassLoader.loadClass(loadOnStartupServletClassName);
                getServlet(clazz);
            }catch (ClassNotFoundException|InstantiationException|IllegalAccessException|ServletException e){
                LogFactory.get().info(e.getMessage());
            }
        }
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
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

    public WebappClassLoader getWebappClassLoader() {
        return webappClassLoader;
    }

    /**
     * @param d
     * @author: ChuShi
     * @date: 2020/6/11 10:42 上午
     * @return: void
     * @desc: 将对应信息从web.xml中解析出来
     */
    private void parseServletMapping(Document d) {
        Elements mappingurlElements = d.select("servlet-mapping url-pattern");
        for (Element mappingurlElement : mappingurlElements) {
            String urlPattern = mappingurlElement.text();
            String servletName = mappingurlElement.parent().select("servlet-name").first().text();
            url_servletName.put(urlPattern, servletName);
        }
        Elements servletNameEmements = d.select("servlet servlet-name");
        for (Element servletNameElement : servletNameEmements) {
            String servletName = servletNameElement.text();
            String servletClass = servletNameElement.parent().select("servlet-class").first().text();
            servletName_className.put(servletName, servletClass);
            className_servletName.put(servletClass, servletName);
        }
        Set<String> urls = url_servletName.keySet();
        for (String url : urls) {
            String servletName = url_servletName.get(url);
            String servletClassName = servletName_className.get(servletName);
            url_servletClassName.put(url, servletClassName);
        }
    }

    private void checkDuplicate(Document d, String mapping, String desc) throws WebConfigDuplicatedException {
        Elements elements = d.select(mapping);
        //判断逻辑释放入一个集合，然后把集合排序后看相邻两个元素是否相同
        List<String> contents = new ArrayList<>();
        for (Element e : elements) {
            contents.add(e.text());
        }
        Collections.sort(contents);
        for (int i = 0; i < contents.size() - 1; i++) {
            String contentPre = contents.get(i);
            String contentNext = contents.get(i + 1);
            if (contentPre.equals(contentNext)) {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, contentPre));
            }
        }
    }

    private void checkDuplicate() throws WebConfigDuplicatedException {
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        checkDuplicate(d, "servlet-mapping url-pattern", "servlet url重复，请保持其唯一性:{}");
        checkDuplicate(d, "servlet servlet-name", "servlet 名称重复，请保持其唯一性:{}");
        checkDuplicate(d, "servlet servlet-class", "servlet 类名重复，请保持其唯一性:{}");
    }

    /**
     * @param
     * @author: ChuShi
     * @date: 2020/6/11 11:09 上午
     * @return: void
     * @desc: 初始化
     */
    private void init() {
        //判断是否有web.xml文件
        if (!contextWebXmlFile.exists()) {
            return;
        }
        try {
            //判断是否重复
            checkDuplicate();
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
            return;
        }
        //进行web.xml的解析
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        parseServletMapping(d);
        parseServletInitParams(d);
        parseLoadOnStartup(d);
        handleLoadOnStartup();
        parseFilterMapping(d);
        parseFilterInitParams(d);
        initFilter();
    }

    private void deploy() {
        init();
        if (reloadable) {
            contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            contextFileChangeWatcher.start();
        }
        //进行JspRuntimeContext的初始化，是为了能够在jsp所转换的Java文件中的javax.servlet.jsp.JspFactory.getDefaultFactory()能够有返回值
        JspC c = new JspC();
        new JspRuntimeContext(servletContext,c);
    }

    public void stop() {
        webappClassLoader.stop();
        contextFileChangeWatcher.stop();
        destroyServlets();
    }

    public void reload() {
        host.reload(this);
    }

    public String getServletClassName(String uri) {
        return url_servletClassName.get(uri);
    }

    private void parseServletInitParams(Document d) {
        Elements servletClassNameElements = d.select("servlet-class");
        for (Element servletClassNameElement : servletClassNameElements) {
            String servletClassName = servletClassNameElement.text();
            Elements initElements = servletClassNameElement.parent().select("init-param");
            if (initElements.isEmpty()) {
                continue;
            }
            Map<String, String> initParams = new HashMap<>();
            for (Element element : initElements) {
                String name = element.select("param-name").get(0).text();
                String value = element.select("param-value").get(0).text();
                initParams.put(name, value);
            }
            servletClassNameInitParams.put(servletClassName, initParams);
        }
        System.out.println("classNameInitParams: " + servletClassNameInitParams);
    }

    private void destroyServlets(){
        Collection<HttpServlet> servlets = servletPool.values();
        for (HttpServlet servlet:servlets){
            servlet.destroy();
        }
    }

    public void parseFilterMapping(Document d){
        //filterUrlName
        Elements mappingurlElements = d.select("filter-mapping url-pattern");
        for (Element mappingurlElement:mappingurlElements){
            String urlPattern = mappingurlElement.text();
            String filterName = mappingurlElement.parent().select("filter-name").first().text();

            List<String> filterNames = urlFilterNames.get(urlPattern);
            if (null==filterNames){
                filterNames = new ArrayList<>();
                urlFilterNames.put(urlPattern,filterNames);
            }
            filterNames.add(filterName);
        }
        //classNameFilterName
        Elements filterNameElements = d.select("filter filter-name");
        for (Element filterNameElement:filterNameElements){
            String fileterName = filterNameElement.text();
            String filterClass = filterNameElement.parent().select("filter-class").first().text();
            filterNameClassName.put(fileterName,filterClass);
            classNameFilterName.put(filterClass,fileterName);
        }

        //urlFilterClassName
        Set<String> urls = urlFilterNames.keySet();
        for (String url:urls){
            List<String> filterNames = urlFilterNames.get(url);
            if (null==filterNames){
                filterNames = new ArrayList<>();
                urlFilterNames.put(url,filterNames);
            }
            for (String filterName:filterNames){
                String filterClassName = filterNameClassName.get(filterName);
                List<String> filterClassNames = urlFilterClassName.get(url);
                if (null==filterClassNames){
                    filterClassNames = new ArrayList<>();
                    urlFilterClassName.put(url,filterClassNames);
                }
                filterClassNames.add(filterClassName);
            }
        }
    }

    private void parseFilterInitParams(Document d){
        Elements filterClassNameElements = d.select("filter-class");
        for (Element filterClassNameElement:filterClassNameElements){
            String filterClassName = filterClassNameElement.text();

            Elements initElements = filterClassNameElement.parent().select("init-param");
            if (initElements.isEmpty()){
                continue;
            }

            Map<String, String> initParams = new HashMap<>();
            for (Element element:initElements){
                String name = element.select("param-name").get(0).text();
                String value = element.select("param-value").get(0).text();
                initParams.put(name,value);
            }

            filterClassNameInitParams.put(filterClassName,initParams);
        }
    }

    private void initFilter(){
        Set<String> classNames = classNameFilterName.keySet();
        for (String className:classNames){
            try {
                Class clazz = this.getWebappClassLoader().loadClass(className);
                Map<String, String> initParameters = filterClassNameInitParams.get(className);
                String filterName = classNameFilterName.get(className);
                FilterConfig filterConfig = new StandardFilterConfig(servletContext,filterName,initParameters);
                Filter filter = filterPool.get(clazz);
                if (null==filter){
                    filter = (Filter) ReflectUtil.newInstance(clazz);
                    filter.init(filterConfig);
                    filterPool.put(className,filter);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
