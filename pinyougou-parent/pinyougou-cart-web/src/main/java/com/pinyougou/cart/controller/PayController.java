package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;


    @Reference
    private OrderService orderService;
    /**
     * 调用pay方法 金额写死 返回pay页面要的信息
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //调用微信pay服务 金额写死1分 不写死了 根据这个用户 从redis中拿这个用户的订单数据了 cart-web中可没有redisTemplate哦去
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //其他有redisTemplate对象的类中拿数据吧 orderservice订单服务嘛 就是要一个支付日记嘛 从订单日志中拿 日志是真正哪里处理结算的
        TbPayLog payLog  = orderService.searchPayLogFromRedis(userId);
        //订单号已经生成了 拿订单日志中的支付订单号
        Map aNative = weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        return aNative;
    }

    /**
     * 待修改  定时查询支付状态
     * @param out_trade_no 这个是订单日志的支付订单号
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        try {
            //调用查询接口
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);

            if (map == null) {
                //出错 查询支付功能出错了
                return new Result(false,"401");//支付失败
            }
            if ("SUCCESS".equals(map.get("trade_state"))){

                //我们要更新真正处理结算的 订单日志为以支付 还是调服务 交易成功 微信那边的流水也传过来 记录下来
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
                //支付成功
                //拿到返回的订单流水更新订单日志
                return new Result(true,"支付成功");

            }
            return new Result(false,"402");//未查询到支付成功
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"401");//系统错误 支付失败
        }


    }


}
