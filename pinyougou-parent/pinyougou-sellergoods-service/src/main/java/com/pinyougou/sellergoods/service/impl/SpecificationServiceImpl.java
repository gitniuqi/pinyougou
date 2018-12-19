package com.pinyougou.sellergoods.service.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		specificationMapper.insert(specification.getSpecification());
		for(TbSpecificationOption specificationOption:specification.getSpecificationOptionList()){
			specificationOption.setSpecId(specification.getSpecification().getId());
			//设置规格ID
            specificationOptionMapper.insert(specificationOption);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//保存修改后的规格
		specificationMapper.updateByPrimaryKey(specification.getSpecification());
		//删除原有的规格选项
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		//指定规格为id
		//这里相当于 添加where specid = (参数)
		criteria.andSpecIdEqualTo(specification.getSpecification().getId());
		//将处理好的参数传入dao方法 这里是先删除所有
		specificationOptionMapper.deleteByExample(example);

		//如何再插入规格选项
		for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
			//将规格的id赋值到规格选项类中
			specificationOption.setSpecId(specification.getSpecification().getId());
			//调用规格选项的insert方法 传入
			specificationOptionMapper.insert(specificationOption);
		}
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//查询规格
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		//查询规格选项列表
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(id);//根据规则id进行查询
		//查询出这个规格id的所有选项 select * from x where specId = id
		List<TbSpecificationOption> optionList  = specificationOptionMapper.selectByExample(example);
		//构建组合实体类返回结果
		Specification spec = new Specification();
		spec.setSpecification(tbSpecification); //往组合实体类中添加 一类规格
		spec.setSpecificationOptionList(optionList); //一种规格的 选项集合
		return spec; //返回
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//根据规格id删除 这类规格
			specificationMapper.deleteByPrimaryKey(id);
			//还要删除这个规格对应的规格选项option表中对应这个规格的数据
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();//创造一个标准
			//通过这个标准指定 将id传入 好比 where specid = id
			criteria.andSpecIdEqualTo(id);
			//将这个标准传入 好比 delete * from x where 'example'
			specificationOptionMapper.deleteByExample(example);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 返回所有规格的id和name集合
	 * @return
	 */
	@Override
	public List<Map> selectSOptionList() {
		//查询到所有的规格
		List<TbSpecification> specifications = specificationMapper.selectByExample(null);
		List<Map> lists = new ArrayList<>();

		for (TbSpecification specification : specifications) {
			Map map = new HashMap<>();
			map.put("id",specification.getId());
			map.put("text",specification.getSpecName());
			lists.add(map);
		}
		return lists;
	}

}
