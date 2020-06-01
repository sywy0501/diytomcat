package com.cs.tomcat.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description: 线程池工具类
 * @author: chushi
 * @create: 2020-06-01 15:31
 **/
public class ThreadPoolUtil {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    public static void run(Runnable r){
        threadPoolExecutor.execute(r);
    }
}
