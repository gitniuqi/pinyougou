package com.pinyougou.shop.security;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 要自定义登录认证类 因为认证信息来自我们自己的数据库了 不再去security设置了
 * 很重要的要继承 userDetailService接口
 * 重新 userDetails方法  userDetails是一个接口 user是他是实现类
 *
 */
public class UserDetailsServiceImpl implements UserDetailsService {


    private SellerService sellerService;
    //当然 我们要操作数据库 就需要操作service层处理数据 就需要注入别的系统的service
    //这个系统是一个消费者 我们要拿生产着


    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    //在自定义登录认证类还要交给spring容器管理
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //他要三个参数 1 username是页面传过来的
        // 2password 是根据username查询数据库的
        // 3GrantedAuthority集合是这个用户的权限

        //调用service层
        TbSeller seller = sellerService.findOne(username);

        if (seller==null){
            return null;
        }
        if (!"1".equals(seller.getStatus())){
            System.out.println("权限不足");
            return null;
        }


        String password = seller.getPassword();


        List<GrantedAuthority> grantedAuthors =new ArrayList<>();

        grantedAuthors.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        User user = new User(username,password,grantedAuthors);
        //将数据库中的信息封装到user对象中 然后返回给security security 会给我们匹配 在保存用户密码时指定加盐加密
        // 并在security的配置文件中指定加密方式 security会自动给我们判断比对
        return user;
    }




}
