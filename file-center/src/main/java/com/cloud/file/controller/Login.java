package com.cloud.file.controller;

import com.cloud.file.utils.ApiResult;
import com.cloud.file.utils.Table;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/login")
public class Login {

    @RequestMapping("/")
    public  String toIndex(){
        return "toLogin";
    }

    @RequestMapping("/toLogin")
    public  String toLogin(){
        System.out.println("aaa");
        return "toLogin";
    }

    @PostMapping("/doLogin")
    @ResponseBody
    public ApiResult doLogin(HttpServletRequest request){
                String name=request.getParameter("name");
                String password=request.getParameter("password");

                if(!Table.user.containsKey(name)){
                    return  ApiResult.fail("账户名不存在");
                }

                if (!Table.user.get(name).equals(password)){
                    return  ApiResult.fail("密码不正确");
                }

                request.getSession().setAttribute("name",name);
             return    ApiResult.success();
    }
}
