package com.pinyougou.sellergoods.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController //相当于resp +controller
@RequestMapping("/login")
public class LoginController {

    /**
     * 获得登陆名
     * @return
     */
    @RequestMapping("/name")
    public Map getName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap<>();
        map.put("loginName",name);
        return map;
    }
}
