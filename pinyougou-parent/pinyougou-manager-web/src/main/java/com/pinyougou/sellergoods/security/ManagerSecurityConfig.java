package com.pinyougou.sellergoods.security;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class ManagerSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/css/**","/img/**","/js/**","/plugins/**","/login.html").permitAll()
                //意思是其他所有请求都需要认证通过即可，用户名密码正确就好不需要角色
                .anyRequest().authenticated();

        //设置表单登陆
        http.formLogin()
                .loginPage("/login.html")//登陆页面
                .loginProcessingUrl("/login")//登陆成功请求
                .defaultSuccessUrl("/admin/index.html",true)//错误跳转页面
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
        auth.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN");
    }
}
