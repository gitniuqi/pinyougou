package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    //这个服务是另外一个系统的
    @Reference
    private OrderService orderService;

    //用户提交订单 结算时 产生订单
    @RequestMapping("/add")
    public Result add(@RequestBody TbOrder order){
        System.out.println(orderService);
        //添加订单到数据库tbOrder中
        //订单中没有用户id
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUserId(userId);
        //订单来源
        order.setSourceType("2");//写死pc
        //调用服务
        try {
            orderService.add(order);
            return new Result(true,"订单生产");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"订单生成失败");
        }


    }
}
