package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private TbOrderMapper orderMapper;

    //添加日志
    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 增加订单到数据库中
     * @param order
     */
    public void add(TbOrder order){
        //得到购物车数据 从redis中拿 页面传的都只是一些订单共有信息
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("REDIS_CART_LIST_KEY_").get(order.getUserId());
        double totalAllMoney = 0;
        List orderList = new ArrayList(); //这是日志列表要的 订单id集合
        //订单详细信息 拆单 根据商家 每一个商家都要有一个订单
        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();//雪花生成订单id
            TbOrder tborder=new TbOrder();//新创建订单对象
            tborder.setOrderId(orderId);//订单ID
            orderList.add(orderId+"");//将订单id添加到集合
            tborder.setUserId(order.getUserId());//用户名
            tborder.setPaymentType(order.getPaymentType());//支付类型
            tborder.setStatus("1");//状态：未付款
            tborder.setCreateTime(new Date());//订单创建日期
            tborder.setUpdateTime(new Date());//订单更新日期
            tborder.setReceiverAreaName(order.getReceiverAreaName());//地址
            tborder.setReceiverMobile(order.getReceiverMobile());//手机号
            tborder.setReceiver(order.getReceiver());//收货人
            tborder.setSourceType(order.getSourceType());//订单来源
            tborder.setSellerId(cart.getSellerId());//商家ID
            //循环购物车明细 变量购物车的 各个订单项 这个是商家的订单
            double money=0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setId(idWorker.nextId());//orderItem表中的id 雪花算法生成了
                orderItem.setOrderId(orderId);//订单id
                orderItem.setSellerId(cart.getSellerId());//商家
                TbItem tbItem = itemMapper.selectByPrimaryKey(orderItem.getItemId());//商品
                orderItem.setGoodsId(tbItem.getGoodsId());
                money+=orderItem.getTotalFee().doubleValue();//金额累加 这个商家要收多少的钱
                orderItemMapper.insert(orderItem);//点击提交订单 将订单信息保存到orderItem中
            }
            //在迭代器中
            totalAllMoney+=money;
            //还有保存 到order列表
            orderMapper.insert(tborder);
        }
        //再生成订单日志
        TbPayLog tbPayLog = new TbPayLog();

        tbPayLog.setOutTradeNo(idWorker.nextId()+"");//这里用空参了不用工作机器id了和序列化了 支付订单号
        tbPayLog.setCreateTime(new Date());//日记创建时间
        long totalFee = (long) (totalAllMoney * 100);
        tbPayLog.setTotalFee(totalFee);//支付总金额 由各个商家的订单相加
        tbPayLog.setUserId(order.getUserId());//获得用户的id
        //交易号 不知道
        tbPayLog.setTradeState("0");//这里未支付
        tbPayLog.setOrderList(orderList.toString().replace("[","").replace("]",""));//是个string类型 拿订单id 上面订单弄个集合传过来吧 去[]
        tbPayLog.setPayType("1");
        payLogMapper.insert(tbPayLog);//加到数据库中
        //再将用户的日志记录储存到redis中 大key是tbPayLog  小key是用户的id value是这个用户的订单日志
        redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).put(order.getUserId(),tbPayLog);

        //清空redis中的清空redis中的
        redisTemplate.boundHashOps("REDIS_CART_LIST_KEY_").delete(order.getUserId());

    }

    /**
     * 从redis中查询为支付的日志信息
     * @param userId
     * @return
     */
    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).get(userId);
    }

    /**
     * 更新日志的支付状态
     * @param out_trade_no
     * @param
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //更新order paylog 的支付状态和 微信支付流水
        //根据日志id查询到这个日志
        TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        tbPayLog.setPayTime(new Date());//现在的是支付时间 new的服务器的时间哦
        tbPayLog.setTradeState("1");//支付状态
        tbPayLog.setTransactionId(transaction_id);//微信流水
        payLogMapper.updateByPrimaryKey(tbPayLog);
        //修改订单状态 order
        //因为order出缓存中移除了 从缓存日志中缓存数据 有订单号列表 转换成对象变量 通过每个订单号查询到订单 修改即可
        String orderList = tbPayLog.getOrderList();
        String[] orderIds = orderList.split(",");
        for (String orderId : orderIds) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
            if (tbOrder != null){
                tbOrder.setStatus("2");//已付款
                orderMapper.updateByPrimaryKey(tbOrder);
            }

        }

        //删除缓存中这个用户的paylog记录
        redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).delete(tbPayLog.getUserId());
    }


}
