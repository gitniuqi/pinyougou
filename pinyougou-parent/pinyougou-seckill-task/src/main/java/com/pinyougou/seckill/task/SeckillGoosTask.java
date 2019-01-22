package com.pinyougou.seckill.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
@Component
public class SeckillGoosTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 每隔30秒来查询商品列表 从数据库中拿
     */
    @Scheduled(cron = "0/5 * * * * ? ")
    public void pushGoods(){
        //1.查询所有的秒杀商品列表   状态审核 没有超过活动期间  库存大于0
        TbSeckillGoodsExample exmaple = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = exmaple.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);//库存大于0

        //    start_time<当前的时间<end_time
        Date date = new Date();
        criteria.andStartTimeLessThan(date);
        criteria.andEndTimeGreaterThan(date);

        //排除 已经在redis中存在商品
        Set<Long> seckillGoods = redisTemplate.boundHashOps("seckillGoods").keys();

        if(seckillGoods!=null &&  seckillGoods.size()>0) {
            List<Long> goodsids = new ArrayList<>();
            for (Long seckillGood : seckillGoods) {
                goodsids.add(seckillGood);
            }
            criteria.andIdNotIn(goodsids);
        }


        List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(exmaple);
        //2.商品的列表存储redis中

        for (TbSeckillGoods tbSeckillGood : tbSeckillGoods) {
            pushGoodsToRedisList(tbSeckillGood);//见商品压入redis栈中
            redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGood.getId(),tbSeckillGood);
        }
    }

    private void pushGoodsToRedisList(TbSeckillGoods tbSeckillGood){
        Integer stockCount = tbSeckillGood.getStockCount();//5
        for (Integer i = 0; i < stockCount; i++) {//队列有5个元素  一个商品就是一个队列 队列的元素大小和库存数据一致
            redisTemplate.boundListOps("seckillGoodsList"+ tbSeckillGood.getId()).leftPush(tbSeckillGood.getId());
        }

    }

}
