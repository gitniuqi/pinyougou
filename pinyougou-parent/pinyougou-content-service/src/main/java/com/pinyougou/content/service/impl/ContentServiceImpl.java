package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层  首页查询 图片(content)数据库 查询首页的图片保存在redis
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 根据广告类型id查询列表 +redis
	 */
	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		System.out.println(categoryId);
		//先从redis中查找数据
		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("TB_CONTENT_REDIS_INDEX_KEY").get(categoryId);//hash map
		System.out.println(contentList);
		if (contentList!=null && contentList.size()>0){
			System.out.println("从缓存读取数据");
			//redis中有数据
			return contentList;
		}
		//去mysql数据库中查
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		criteria.andStatusEqualTo("1");//开启状态
		example.setOrderByClause("sort_order desc");//设置排序代替是order by   + sort_order desc
		List<TbContent> tbContents = contentMapper.selectByExample(example);
		//将mysql查到的数据存到redis中
		redisTemplate.boundHashOps("TB_CONTENT_REDIS_INDEX_KEY").put(categoryId,tbContents);
		System.out.println("从数据库读取数据放入缓存");
		return tbContents;
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加 信息后要同步redis
	 */
	@Override
	public void add(TbContent content) {
		redisTemplate.boundHashOps("TB_CONTENT_REDIS_INDEX_KEY").delete(content.getCategoryId());//清空缓存
		contentMapper.insert(content);

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//1获取用来的广告的分类id
		Long id = content.getId();
		TbContent tbContent = contentMapper.selectByPrimaryKey(id);
		Long categoryId = tbContent.getCategoryId();
		//2再获取现在的广告的分类id
		Long categoryId1 = content.getCategoryId();
		contentMapper.updateByPrimaryKey(content);

		//判断是否更新了分类 清空
		if (categoryId1!=categoryId.longValue()){
			redisTemplate.boundHashOps("TB_CONTENT_REDIS_INDEX_KEY").delete(categoryId1);
			redisTemplate.boundHashOps("TB_CONTENT_REDIS_INDEX_KEY").delete(categoryId);
		}else {
			redisTemplate.boundHashOps("TB_CONTENT_REDIS_INDEX_KEY").delete(categoryId);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//先查询到改广告对应的分类id
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			contentMapper.deleteByPrimaryKey(id);
			//根据广告分类id删除redis中的数据
			redisTemplate.boundHashOps("TB_CONTENT_REDIS_INDEX_KEY").delete(tbContent.getCategoryId());
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getContent()!=null && content.getContent().length()>0){
				criteria.andContentLike("%"+content.getContent()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
