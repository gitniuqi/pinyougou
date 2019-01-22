package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.print.attribute.HashAttributeSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
    //登录交给了cas单点登陆做了
    @RequestMapping("/name")
    public Map showName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();//获得登陆name
        Map map = new HashMap();
        map.put("username",name);
        return map;

    }
}
