package com.cs.tomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * @description:
 * @author: chushi
 * @create: 2020-08-11 11:04
 **/
public class SessionManager {
    //存放所有session
    private static Map<String, StandardSession> sessionMap = new HashMap<>();
    //session的默认失效时间
    private static int defaultTimeout = getTimeout();

    //默认启动检测session是否失效的线程
    static {
        startSessionOutdateCheckThread();
    }

    /**
     * @param
     * @author: ChuShi
     * @date: 2020/8/11 2:10 下午
     * @return: int
     * @desc: 从web.xml中获取默认失效时间
     */
    private static int getTimeout() {
        int defaultResult = 30;
        try {
            Document d = Jsoup.parse(Constant.webXmlFile, "utf-8");
            Elements es = d.select("session-config session-timeout");
            if (es.isEmpty()) {
                return defaultResult;
            }
            return Convert.toInt(es.get(0).text());
        } catch (IOException e) {
            return defaultResult;
        }
    }

    /**
     * @param jsessionid
     * @param request
     * @param response
     * @author: ChuShi
     * @date: 2020/8/11 2:12 下午
     * @return: javax.servlet.http.HttpSession
     * @desc: 如果浏览器没有传jsessionid过来，就创建一个新的session
     * 如果浏览器传递过来的jsessionid无效，那么也创建一个新的sessionid
     * 否则就使用现成的session，并修改他的lastAccessedTime，以及创建对应的cookie
     */
    public static HttpSession getSession(String jsessionid, Request request, Response response) {
        if (null == jsessionid) {
            return newSession(request, response);
        } else {
            StandardSession currentSession = sessionMap.get(jsessionid);
            if (null == currentSession) {
                return newSession(request, response);
            } else {
                currentSession.setLastAccessedTime(System.currentTimeMillis());
                createCookieBySession(currentSession, request, response);
                return currentSession;
            }
        }
    }

    private static void createCookieBySession(HttpSession session, Request request, Response response) {
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }

    /**
     * @param request
     * @param response
     * @author: ChuShi
     * @date: 2020/8/11 2:17 下午
     * @return: javax.servlet.http.HttpSession
     * @desc: 创建session
     */
    private static HttpSession newSession(Request request, Response response) {
        ServletContext servletContext = request.getServletContext();
        String sid = generateSessionId();
        StandardSession session = new StandardSession(sid, servletContext);
        session.setMaxInactiveInterval(defaultTimeout);
        sessionMap.put(sid, session);
        createCookieBySession(session, request, response);
        return session;
    }

    /**
     * @param
     * @author: ChuShi
     * @date: 2020/8/11 2:13 下午
     * @return: void
     * @desc: 从sessionMap里面根据lastAccessTime筛选出过期的jsessionids，然后把他们从sessionMap里去掉
     */
    private static void checkOutDateSession() {
        Set<String> jsessionids = sessionMap.keySet();
        List<String> outdateJessionIds = new ArrayList<>();

        for (String jsessionId : jsessionids) {
            StandardSession session = sessionMap.get(jsessionId);
            long interval = System.currentTimeMillis() - session.getLastAccessedTime();
            if (interval > session.getMaxInactiveInterval() * 1000) {
                outdateJessionIds.add(jsessionId);
            }
        }

        for (String jsessionId : outdateJessionIds) {
            sessionMap.remove(jsessionId);
        }
    }

    /**
     * @param
     * @author: ChuShi
     * @date: 2020/8/11 2:11 下午
     * @return: void
     * @desc: 启动线程，每隔30s调用一次checkoutdatesession方法
     */
    private static void startSessionOutdateCheckThread() {
        new Thread() {
            public void run() {
                while (true) {
                    checkOutDateSession();
                    ThreadUtil.sleep(1000 * 3);
                }
            }
        }.start();
    }

    /**
     * @param
     * @author: ChuShi
     * @date: 2020/8/11 2:11 下午
     * @return: java.lang.String
     * @desc: 创建sessionId
     */
    public static synchronized String generateSessionId() {
        String result = null;
        byte[] bytes = RandomUtil.randomBytes(16);
        result = new String(bytes);
        result = SecureUtil.md5(result);
        result = result.toUpperCase();
        return result;
    }
}
