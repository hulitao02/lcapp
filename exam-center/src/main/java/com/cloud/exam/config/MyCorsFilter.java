package com.cloud.exam.config;


import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
//@Component
public class MyCorsFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) request;
        httpServletResponse.setHeader("Access-Control-Allow-Origin","*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods","POST,GET,OPTIONS,DELETE");
        httpServletResponse.setHeader("Access-Control-Max-Age","36000");
        httpServletResponse.setHeader("Access-Control-Allow-Headers","x-requested-with,content-type");
        chain.doFilter(request, response);










    }

    @Override
    public void destroy() {

    }
}
