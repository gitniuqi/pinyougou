package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车中
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    List<Cart> findCartListFromRedis(String username);

    void addCartListToRedis(List<Cart> cartList,String username);

    List<Cart> merge(List<Cart> cookieCartList, List<Cart> redisCartList);
}
