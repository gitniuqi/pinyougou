package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pinyougou.sellergoods.service.ItemCatService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 通过传入parentId查找到 是这个父亲的所有儿子                            key   resultMap categoryList
	 * 在查询页面 查询关键字 查询item表的title字段的关键字 找到所有对应关键字的item字段，先保存到集合中显示 然后分组查询的这些字段的
	 * 分类名称放在(categoryList)中 我们要在页面的商品显示该商品的有那些分类，并把这些分类下的品牌和规格查询出来 先通过模板表查询到这个
	 * 模板对应的模板id(itemcat) 然后由(tb_type_template)表查询到这个模板id下有的品牌和规格（json数据）保存到缓存中，用于回现
	 *
	 * @param parentId
	 * @return
	 */
	//因为增删改查后都会刷新 加载页面 所以在这里更新redis就好
	@Override
	public List<TbItemCat> findByParentId(Long parentId) {
		//不是查id 主键
		//select * from tbitemcat where parentid = ?
		TbItemCatExample example = new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId); //相当于 添加where parentid = parentId
		//1读取都所有的itemcat数据在list中 最新的itemcat哦 cued过的哦
		List<TbItemCat> all = findAll();
		//遍历
		for (TbItemCat tbItemCat : all) {//在redis中hash itemCat是表 key,value是put(key,value)
			redisTemplate.boundHashOps("itemCat").put(tbItemCat.getName(),tbItemCat.getTypeId());//全部保存了
		}
		System.out.println("更新缓存：商品分类表");
		List<TbItemCat> lists = itemCatMapper.selectByExample(example);
		return lists;
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			itemCatMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
