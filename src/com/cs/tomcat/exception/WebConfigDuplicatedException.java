package com.cs.tomcat.exception;

/**
 * @description: 针对配置文件的自定义异常 servlet重复配置时抛出
 * @author: chushi
 * @create: 2020-06-09 16:45
 **/
public class WebConfigDuplicatedException extends Exception{

    public WebConfigDuplicatedException(String msg){
        super(msg);
    }
}
