package com.pinyougou.seckill.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {

    @RequestMapping("/page/login")
    public String login(String url){
        System.out.println("=======+"+url);
        return "redirect:"+url;
    }
}
