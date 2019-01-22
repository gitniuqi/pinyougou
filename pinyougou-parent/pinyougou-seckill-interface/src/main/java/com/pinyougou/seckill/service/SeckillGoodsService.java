package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;

import java.util.List;

public interface SeckillGoodsService {
    /**
     * 查询所有秒杀商品
     * @return
     */
    List<TbSeckillGoods> findList();

    /**
     * 根据url的id查询redis中的那一个tbseckillgoods
     * @param id
     * @return
     */
    TbSeckillGoods findOne(Long id);

    /**
     * 下订单请求
     * @param seckillId
     * @param userId
     */
    void submitOrder(Long seckillId, String userId);

    /**
     * 订单服务 根据用户获得订单信息
     * @param userId
     * @return
     */
    TbSeckillOrder getSeckillOrderByUserId(String userId);

    /**
     * 检测到订单支付成功 修改订单状态 并从缓存中删除信息
     * @param out_trade_no
     * @param userId
     * @param s
     */
    void updateStatus(String out_trade_no, String userId, String s);
}
