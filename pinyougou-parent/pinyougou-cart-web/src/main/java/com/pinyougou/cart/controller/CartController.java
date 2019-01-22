package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.utils.CookieUtil;
import entity.Result;


import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;
    
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(HttpServletRequest request, HttpServletResponse response,Long itemId, Integer num){
        try {
            //解决跨域请求
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//统一指定的域访问我的服务器资源
            response.setHeader("Access-Control-Allow-Credentials", "true");//同意客户端携带cookie
            //判断用户名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(name)){  //anonymousUser是匿名用户
              //没有登录
                //2.1 先获取cookie中购物车列表
                String cartListstring = CookieUtil.getCookieValue(request, "cartList", true);
                List cartList = new ArrayList();
                //将购物车列表转换成list集合 //判断是否为空
                if (StringUtils.isNotBlank(cartListstring)){
                    cartList = JSON.parseArray(cartListstring,Cart.class);
                }
                //一个新的购物车集合
                cartList = cartService.addGoodsToCartList(cartList, itemId, num);
                CookieUtil.setCookie(request,response,"cartList", JSON.toJSONString(cartList),7*24*3600,true);
                }else {
                    //已经登录操作的是redis
                    //3.1 先从redis中获取已有的购物车的列表数据
                    List<Cart> cartListFromRedisold = cartService.findCartListFromRedis(name);
                    //3.2 向已有的购物车列表中添加商品  返回一个最新的购物车列表
                    List<Cart> cartnew =  cartService.addGoodsToCartList(cartListFromRedisold,itemId,num);
                    //3.3 重新设置回redis中
                    cartService.addCartListToRedis(cartnew,name);
                }
                return new Result(true,"操作成功");
            } catch (Exception e) {
                e.printStackTrace();
                return new Result(false,"操作失败");
        }
    }

    //合并
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){
        //获得当前用户的用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if("anonymousUser".equals(name)) {
            //没有登录
            String cartListstring = CookieUtil.getCookieValue(request, "cartList", true);
            List<Cart> cartList = new ArrayList();
            if (StringUtils.isNotBlank(cartListstring)) {//redis中有数据
                cartList = JSON.parseArray(cartListstring, Cart.class);
            }
            return cartList;
        }else {
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(name);//从redis中获得数据
            //合并cookie中的数据
            //从cookie中找
            String cartListstring = CookieUtil.getCookieValue(request, "cartList", true);
            List<Cart> cookieList = new ArrayList<>();
            if(StringUtils.isNotBlank(cartListstring)){
                cookieList=JSON.parseArray(cartListstring, Cart.class);
            }
            List<Cart> cartMostNew = cartService.merge(cookieList,cartListFromRedis);
            //将最新的购物车列表数据 保存到redis中
            cartService.addCartListToRedis(cartMostNew,name);
            //5.重新查询redis中的数据返回
            //cartMostNew
            //6.清空cookie中的购物车数据
            CookieUtil.deleteCookie(request,response,"cartList");
            return cartMostNew;
        }
    }

}
