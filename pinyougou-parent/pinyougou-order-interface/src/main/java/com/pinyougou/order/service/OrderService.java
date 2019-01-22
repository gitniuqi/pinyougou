package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;

public interface OrderService {
    /**
     * 添加订单到数据库中
     * @param order
     */
    void add(TbOrder order);

    /**
     * 从redis中查询日志订单
     * @param userId
     * @return
     */
    TbPayLog searchPayLogFromRedis(String userId);

    /**
     * 更新真正处理交易的日志支付状态
     * @param out_trade_no
     * @param s
     */
    void updateOrderStatus(String out_trade_no, String s);
}
