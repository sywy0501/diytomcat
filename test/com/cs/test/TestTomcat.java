package com.cs.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.util.MiniBrowser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @description: 测试类
 * @author: chushi
 * @create: 2020-05-29 15:43
 **/
public class TestTomcat {
    private static int port = 18080;
    private static String ip = "127.0.0.1";
    @BeforeClass
    public static void beforeClass(){

        if (NetUtil.isUsableLocalPort(port)){
            System.out.println("请先启动 位于端口："+port+" 的diy tomcat，否则无法进行单元测试");
            System.exit(1);
        }else {
            System.out.println("检测到diy tomcat已经启动，开始进行单元测试");
        }
    }

    @Test
    public void testHelloTomcat(){
        String html = getContentString("/");
        Assert.assertEquals(html,"<div style='color:blue' >Hello DIY Tomcat!</div>");
    }

    @Test
    public void testHtml(){
        String html = getContentString("/a.html");
        Assert.assertEquals(html,"Hello DIY Tomcat from a.html");
    }

    @Test
    public void testTImeConsumeHtml()throws InterruptedException{

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20,20 ,60 ,TimeUnit.SECONDS, new LinkedBlockingDeque<>(10));
        TimeInterval timeInterval = DateUtil.timer();

        for (int i = 0;i<3;i++){
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    getContentString("/timeConsume.html");
                }
            });
        }
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(1,TimeUnit.HOURS);
        long duration = timeInterval.intervalMs();
        Assert.assertTrue(duration<3000);
    }

    @Test
    public void testaIndex(){
        String html = "/a/index.html";
        Assert.assertEquals(getContentString(html),"Hello DIY Tomcat from index.html@a");
    }

    @Test
    public void testbIndex(){
        String html = "/b/index.html";
        Assert.assertEquals(getContentString(html),"Hello DIY Tomcat from index.html@b");
    }

    private String getContentString(String uri){
        String url = StrUtil.format("http://{}:{}{}",ip,port,uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }
}
