package com.cs.test;

import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.util.MiniBrowser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


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

    private String getContentString(String uri){
        String url = StrUtil.format("http://{}:{}{}",ip,port,uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }
}
