package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.utils.HttpClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private  String appid;
    //财付通平台的商户账号
    @Value("${partner}")
    private   String partner;
    //财付通平台的商户密钥
    @Value("${partnerkey}")
    private String partnerkey;
    //回调URL
    @Value("${notifyurl}")
    private String notifyurl;
    /**
     * 创建微信支付二维码
     * @param out_trade_no 订单号
     * @param total_fee 金额
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //调用微信统一下单api 需要设置一些参数
        Map paramMap = new HashMap();
        //1.组合参数 组成一个MAP
        paramMap.put("appid",appid);
        paramMap.put("mch_id",partner);
        //2.生成随机数（使用SDK来做）
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        paramMap.put("body", "品优购");//商品描述
        paramMap.put("out_trade_no", out_trade_no);//商品订单号
        paramMap.put("total_fee", total_fee);//单位是分
        paramMap.put("spbill_create_ip", "127.0.0.1");//终端IP
        paramMap.put("notify_url", notifyurl);//回调地址(随便写)
        paramMap.put("trade_type", "NATIVE");//交易类型
        // //3.生成签名 （使用SKU来做自动的添加签名）
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //4.使用httpclient 模拟浏览器发送https请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);//实体字符串
            httpClient.post();//通过post请求
            //5.使用httpclient 模拟浏览器接收相应
            String resultXmlString = httpClient.getContent();//微信支付系统返回的相应
            System.out.println(resultXmlString);
            //6取出响应中的二维吗链接地址 组合成Map 返回
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXmlString);//将xmltomap
            //我们要是的code_url
            Map map = new HashMap();
            //6.取出响应中的二维码连接地址 组合成MAp 返回
            map.put("code_url",resultMap.get("code_url"));//字符地址 返回这个map到页面转换成二维码
            map.put("total_fee",total_fee);//总金额
            map.put("out_trade_no",out_trade_no);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    /**
     * 监控支付状态
     * @param out_trade_no
     * @return
     */
    @Override
    public  Map queryPayStatus(String out_trade_no) {
        try {
            Map map = new HashMap();
            //1组合参数
            map.put("appid", appid);
            map.put("mch_id",partner);
            //2生成随机数 （使用SDK来做）
            map.put("nonce_str",WXPayUtil.generateNonceStr());//生成随机字符串
            map.put("out_trade_no",out_trade_no);

            //3生成签名 用sku
            String paramXml = WXPayUtil.generateSignedXml(map, partnerkey);
            //4.使用httpclient 模拟浏览器发送https请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");//相当打开浏览器输入地址
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);//实体字符创
            httpClient.post();//通过post请求 按住回车键 发送请求
            //5.使用httpclient 模拟浏览器接收相应
            String resultXmlString = httpClient.getContent();//微信支付系统返回的相应
            //System.out.println("这里是查询支付状态返回的响应"+resultXmlString);
            //6.取出响应中的二维码连接地址 组合成MAp 返回
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXmlString);

            System.out.println("返回的map"+resultMap);
            System.out.println("返回是状态"+resultMap.get("trade_state"));
            System.out.println("SUCCESS".equals(resultMap.get("trade_state")));

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }




}
