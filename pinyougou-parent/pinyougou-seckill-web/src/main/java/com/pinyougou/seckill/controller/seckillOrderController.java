package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.seckill.service.SeckillGoodsService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckillOrder")
public class seckillOrderController {

    @Reference
    private SeckillGoodsService seckillGoodsService;
    /**
     * 下秒杀订单的请求
     * @param seckillId
     * @return
     */
    @RequestMapping("/submitOrder")
    public Result submitOrder(Long seckillId){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)){//匿名用户
            return new Result(false,"用户没有登陆");
        }

        try {
            seckillGoodsService.submitOrder(seckillId,userId);
            return new Result(true,"提交成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"提交失败");
        }
    }

}
