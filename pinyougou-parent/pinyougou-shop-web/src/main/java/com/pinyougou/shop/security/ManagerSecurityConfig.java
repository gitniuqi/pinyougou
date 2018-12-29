package com.pinyougou.shop.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

//这里是security的配置类 等价于security的xml配置文件
@EnableWebSecurity
public class ManagerSecurityConfig extends WebSecurityConfigurerAdapter {

    //我们自定义了登录认证类 在xml中要指定security登录去找我们自定义的登录认证 不用自己设置了
    //首先我们要将这个类注入进这个配置类 就注入他的接口
    @Autowired
    private UserDetailsService userDetailsService;


    //在配置类中注入加盐加密类
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //还注意 add.do的请求也不要拦截 因为比如注册 本来没有账号 就不要让他登录啊
        http.authorizeRequests()
                .antMatchers("/css/**","/img/**","/js/**","/plugins/**","/*.html","/seller/add.do").permitAll()
                //意思是其他所有请求都需要认证通过即可，用户名密码正确就好不需要角色
                .anyRequest().authenticated();

        //设置表单登陆
        http.formLogin()
                .loginPage("/shoplogin.html")//登陆页面
                .loginProcessingUrl("/login")//登陆成功请求
                .defaultSuccessUrl("/admin/index.html",true)//默认成功跳转页面
                .failureUrl("/login?error");//错误的请求路径

        //表单退出登录
        http.logout().logoutUrl("/logout").invalidateHttpSession(true);
        //开启同源可以访问
        http.headers().frameOptions().sameOrigin();
        http.csrf().disable();//关闭csrf
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //会自动添加ROLE_
        //注释掉 不要写死这个用户名了 我们自定义了登录认证
       /* auth.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN");*/
       auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
}
