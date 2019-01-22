package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.service.AddressService;
import com.pinyougou.pojo.TbAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    /*addressService是在Catservice接口中的服务*/
    @Reference
    private AddressService addressService;

    @RequestMapping("/findListByLoginUser")
    public List<TbAddress> findListByloginUser(){
        //从secutityContextHolder中拿用户id
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<TbAddress> listByUserId = addressService.findListByUserId(userId);//查询用户地址集合
        return listByUserId;
    }

}
