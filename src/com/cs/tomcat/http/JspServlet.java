package com.cs.tomcat.http;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.classloader.JspClassLoader;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.JspUtil;
import com.cs.tomcat.util.WebXMLUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @description:
 * @author: chushi
 * @create: 2020-08-12 11:48
 **/
public class JspServlet extends HttpServlet {

    private static JspServlet instance = new JspServlet();

    public static synchronized JspServlet getInstance() {
        return instance;
    }

    private JspServlet() {
    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        try {
            Request request = (Request) httpServletRequest;
            Response response = (Response) httpServletResponse;

            String uri = request.getRequestURI();

            if ("/".equals(uri)) {
                uri = WebXMLUtil.getWelcomeFile(request.getContext());
            }

            String fileName = StrUtil.removePrefix(uri, "/");
            File file = FileUtil.file(request.getRealPath(fileName));

            File jspFile = file;
            if (jspFile.exists()) {
                Context context = request.getContext();
                String path = context.getPath();
                String subFolder;

                if ("/".equals(path)) {
                    subFolder = "_";
                } else {
                    subFolder = StrUtil.subAfter(path, '/', false);
                }

                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);

                if (!jspServletClassFile.exists()) {
                    JspUtil.compileJsp(context, jspFile);
                } else if (jspFile.lastModified() > jspServletClassFile.lastModified()) {
                    //当发现jsp更新之后，就会调用invalidJspClassLoader与之前的JspClassLoader脱钩
                    JspUtil.compileJsp(context, jspFile);
                    JspClassLoader.invalidJspClassLoader(uri,context);
                }
                String extName = FileUtil.extName(file);
                String mimeType = WebXMLUtil.getMimeType(extName);
                response.setContentType(mimeType);

                //根据uri和context获取当前jsp对应的JspClassLoader
                JspClassLoader jspClassLoader = JspClassLoader.getJspClassLoader(uri,context);
                //获取jsp对应的servletClassName
                String jspServletClassName = JspUtil.getJspServletClassName(uri,subFolder);
                //通过JspClassLoader根据servletClassName加载类对象jspServletClass
                Class jspServletClass = jspClassLoader.loadClass(jspServletClassName);
                //使用context的getServlet获取servlet实例
                HttpServlet servlet = context.getServlet(jspServletClass);
                servlet.service(request, response);
                if(null!=response.getRedirectPath())
                    response.setStatus(Constant.CODE_302);
                else
                    response.setStatus(Constant.CODE_200);
            } else {
                response.setStatus(Constant.CODE_404);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
