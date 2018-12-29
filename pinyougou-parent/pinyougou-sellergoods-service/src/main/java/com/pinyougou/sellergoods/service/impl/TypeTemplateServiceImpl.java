package com.pinyougou.sellergoods.service.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;


	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper; //这里在dao系统中 通过mydatis的配置文件扫描dao/mapper包注册

	/**
	 * 查询这个id类型模板的多个规格 及这多个规格对应的多个选项
	 * 因为在id类型模板typetemplate中有他对应的规格数据 切规格[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
	 * 所以不要查询规格表了 直接拿着规格id查改id下对应的多个选项
	 * 首先要注入 id选项的mappeer dao
	 * @param id
	 * @return
	 */
	@Override
	public List<Map> findSpecList(Long id) {
		//通过id查询到模板 这个id是typeTemplate的id
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//JSON.parseArray将json数据封装转换到第二个参数的类型的对象中
		//[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
		List<Map> list = JSON.parseArray(tbTypeTemplate.getSpecIds(), Map.class);
		//遍历这个集合
		for (Map map : list) { //map key id vaule 27
			//根据类型模板id查询 这个模板id下对应的多个选项
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			//我要TbSpecificationOption的id
			//json里面拿的是integer
			Integer specId = (Integer) map.get("id");
			criteria.andSpecIdEqualTo(Long.valueOf(specId));
			List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);//select * from x where specid
			//[id:1,optionName:'4G']
			//3拼接
			map.put("options",options);
		}
		return list;
	}

	/**
	 * 查询所有的类型模板id
	 * @return
	 */
	@Override
	public List<Map> selectAllTypeid() {
        List<TbTypeTemplate> tbTypeTemplates = typeTemplateMapper.selectByExample(null);
        List<Map> list = new ArrayList<>();
        for (TbTypeTemplate tbTypeTemplate : tbTypeTemplates) {
            Map map = new HashMap<>();
            map.put("id",tbTypeTemplate.getId());
            list.add(map);
        }
        return list;
	}


	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}



}
