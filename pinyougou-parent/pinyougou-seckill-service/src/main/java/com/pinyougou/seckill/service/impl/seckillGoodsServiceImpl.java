package com.pinyougou.seckill.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillGoodsService;
import com.pinyougou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

@Service
public class seckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    /**
     * 查询所有秒杀商品
     * @return
     */
    @Override
    public List<TbSeckillGoods> findList() {
        //从缓存中拿 就好了 因为开启了定时任务
        return redisTemplate.boundHashOps("seckillGoods").values();//拿所有的数据
    }

    /**
     * 根据id查询redis
     * @param id
     * @return
     */
    @Override
    public TbSeckillGoods findOne(Long id) {
        return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
    }


    /**
     * 下订单请求 订单 商品服务全在这里了
     * @param seckillId
     * @param userId
     */

    public void submitOrder(Long seckillId, String userId) {
        //先从缓存中拿
        TbSeckillGoods killGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);//根据商品id

        //先进先出的特点 从缓存队列里拿
        Object o = redisTemplate.boundListOps("seckillGoodsList" + seckillId).rightPop();

        if(o==null){
            throw new RuntimeException("已经售罄");

        }
       /* if (killGoods == null || killGoods.getStockCount()<= 0){
            //表示没有库存了
            throw new RuntimeException("商品已经被抢光");
        }*/
        //将这个商品的库存减少
        killGoods.setStockCount(killGoods.getStockCount()-1);//-1
        redisTemplate.boundHashOps("seckillGoods").put(seckillId,killGoods);

        //如果商品被抢光
        if (killGoods.getStockCount()<=0){
            seckillGoodsMapper.updateByPrimaryKey(killGoods);//同步到数据中
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);//删除缓存数据
        }
        //减少成功 创建订单
        long orderId = idWorker.nextId();
        TbSeckillOrder tbSeckillOrder = new TbSeckillOrder();
        tbSeckillOrder.setId(orderId);//创建秒杀订单的id 操作的tbseckilOrder表
        tbSeckillOrder.setCreateTime(new Date());//订单创建时间
        tbSeckillOrder.setMoney(killGoods.getCostPrice());//秒杀价格
        tbSeckillOrder.setSeckillId(seckillId);//秒杀商品的id
        tbSeckillOrder.setSellerId(killGoods.getSellerId());
        tbSeckillOrder.setUserId(userId);//设置用户ID
        tbSeckillOrder.setStatus("0");//状态 未支付

        //将订单先存到缓存中 大K为 seckillOrder
        redisTemplate.boundHashOps("seckillOrder").put(userId,tbSeckillOrder);

    }

    /**
     * 订单服务写到商品服务中 根据用户获得订单信息
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder getSeckillOrderByUserId(String userId) {
        //seckillOrder 是保存到缓存中的订单大k
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public void updateStatus(String out_trade_no, String userId, String transaction_id) {
        //1.先获取订单
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if(seckillOrder==null){
            throw new RuntimeException("订单不存在");
        }
        if(!out_trade_no.equals(seckillOrder.getId().toString())){
            throw new RuntimeException("订单不一致");
        }
        //2.更新订单的状态
        seckillOrder.setPayTime(new Date());//支付时间
        seckillOrder.setStatus("1");//已经支付
        seckillOrder.setTransactionId(transaction_id);

        //3.保存到数据库中
        seckillOrderMapper.insert(seckillOrder);
        //4.清空订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }
}
