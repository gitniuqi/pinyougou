package com.pinyougou.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import java.util.Arrays;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Reference //这个注解才去bubbo注册中心去找服务
	private ItemSearchService searchService;

	@Reference
	private ItemPageService pageService;

	/**
	 *
	 */
	@Autowired
	private JmsTemplate jmsTemplate;

	@Resource(name="solr_update_index_queue")
	private Destination destination;


	@Resource(name="freemarker_gen_html_topic")
	private Destination destinationtopic;



	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status){
		try {
			goodsService.updateStatus(ids,status);
			//按照spu id 查询 sku列表(状态为1)
			if(status.equals("1")){//审核通过
				List<TbItem> itemList = goodsService.findItemListByGoodsId(ids);
				//调用搜索接口实现数据的批量导入
				if(itemList.size()>0){
                    //searchService.updateIndex(itemList);
					//跟新索引库的同时 生成html
					jmsTemplate.send(destination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							String jsonString = JSON.toJSONString(itemList);
							return session.createTextMessage(jsonString);
						}
					});

					//调用生成静态页面的服务
					jmsTemplate.send(destinationtopic, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createObjectMessage(ids);
						}
					});


				}else {
					System.out.println("更新索引库失败");
				}
			}
			return new Result(true,"状态修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"状态修改失败");
		}
	}

	@RequestMapping("/genhtmltest")
	public String genHtml(Long id){
	    pageService.genHtml(id);
	    return "success";
	}
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		//还要获取这个商家的登录用户名
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.getGoods().setSellerId(sellerId);
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			List<Long> longs = Arrays.asList(ids);
			searchService.deleteByGoodsIds(longs); //要一个list集合 我们有一个数组 数组转集合
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	
}
