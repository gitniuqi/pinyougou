package com.pinyougou.cart.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;
    /**
     * 添加商品到购物车中
     * @param cartList
     * @param itemId
     * @param num
     * @return 要将新的购物车集合保存到cookie redis中
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1.根据商品SKUID查询SKU商品信息 主要店家id名
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        //2.获取商家ID
        String sellerId = tbItem.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车的方法
        Cart cart = findCartBySellerId(sellerId,cartList);
        //4.如果购物车列表中不存在该商家的购物车
        System.out.println(cart);
        if (cart==null){
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);//添加商家id
            cart.setSellerName(tbItem.getSeller());//添加商品name

            List<TbOrderItem> tbOrderItems = new ArrayList();
            TbOrderItem tbOrderItem = new TbOrderItem();

            tbOrderItem.setItemId(itemId);//订单SpU ID
            tbOrderItem.setGoodsId(tbItem.getGoodsId());//订单SKU ID
            tbOrderItem.setNum(num);//数量
            tbOrderItem.setPicPath(tbItem.getImage());//图片
            tbOrderItem.setPrice(tbItem.getPrice());//价格
            tbOrderItem.setSellerId(sellerId);//卖家
            tbOrderItem.setTitle(tbItem.getTitle());//商品的标题
            double v = num * (tbItem.getPrice().doubleValue());
            tbOrderItem.setTotalFee(new BigDecimal(v));//小计
            tbOrderItems.add(tbOrderItem);

            cart.setOrderItemList(tbOrderItems);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        }else{
            //5.如果购物车列表中存在该商家的购物车

            // 查询购物车明细列表中是否存在该商品
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem orderItem = searchOrderItemByItemId(orderItemList,itemId);
            if (orderItem==null){
                //5.1. 如果没有，新增购物车明细
                //5.1 判断 要添加的商品 是否在该商家的购物车明细列表存在  如果 没有 ，直接添加商品
                orderItem= new TbOrderItem();
                orderItem.setItemId(itemId);
                orderItem.setGoodsId(tbItem.getGoodsId());
                orderItem.setNum(num);
                orderItem.setPicPath(tbItem.getImage());//图片
                orderItem.setPrice(tbItem.getPrice());//价格
                orderItem.setSellerId(sellerId);//卖家
                orderItem.setTitle(tbItem.getTitle());//商品的标题
                double v = num * (tbItem.getPrice().doubleValue());
                orderItem.setTotalFee(new BigDecimal(v));//小计
                orderItemList.add(orderItem);
            }else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                //金额重新计算
                double v = orderItem.getNum() * (orderItem.getPrice().doubleValue());
                orderItem.setTotalFee(BigDecimal.valueOf(v));
                //如果减少到没有
                if (orderItem.getNum()<=0){
                    //从购物车中移除该购物购物项
                    orderItemList.remove(orderItem);
                }
                //该购物车为0
                if (orderItemList.size()<=0){
                    //从购物车集合中移除该购物车
                    cartList.remove(orderItemList);
                }
            }
        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 从redis中查找cartList
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("REDIS_CART_LIST_KEY_").get(username);
        if (cartList==null){
            return new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 往redis中添加cartList
     * @param cartList
     * @param username
     */
    @Override
    public void addCartListToRedis(List<Cart> cartList, String username) {
        redisTemplate.boundHashOps("REDIS_CART_LIST_KEY_").put(username,cartList);
    }

    //合并


    /**
     * 遍历购物车 查找对应的购物项
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (itemId.equals(orderItem.getItemId())){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 遍历购物车集合 查找对应的购物车
     * @param sellerId
     * @param cartList
     * @return
     */
    private Cart findCartBySellerId(String sellerId, List<Cart> cartList) {

        for (Cart cart : cartList) {
            System.out.println(cart.getSellerId());
            if (cart.getSellerId().equals(sellerId)){
                return cart;//返回购物车
            }
        }
        return null;
    }

    /**
     * 合并
     * @param cookieCartList
     * @param redisCartList
     * @return
     */
    public List<Cart> merge(List<Cart> cookieCartList,List<Cart> redisCartList){
        for (Cart cart : cookieCartList) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                redisCartList = addGoodsToCartList(redisCartList,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return redisCartList;
    }

}
