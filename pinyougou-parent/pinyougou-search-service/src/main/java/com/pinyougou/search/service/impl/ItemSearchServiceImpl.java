package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;



import java.util.*;

@Service //用dubbo的将service发布到zookeeper上 配置文件<dubbo:annotation package在加载的时候扫了的
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate; //不调dao了 直接调solr框架中的工具类从这里面拿

    //对于查询的品牌和各个规格的数据 我们不从solr中查出来拼接了
    //从reids中拿
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索商品
     */
    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map = new HashMap<>();

        //关键字空格处理 去空格覆盖
        String keywords = (String) searchMap.get("keywords");
        keywords=keywords.replace(" ","");
        searchMap.put("keywords", keywords);
        //1高亮查询
        Map resultMap = searchList(searchMap); //查询{key:rows,value:当前页的高亮数据List<TbItem>}
        map.putAll(resultMap);
        //2分组查询 查询商品分类
        List<String> categoryList = searchCategoryList(searchMap); //{手机，笔记本}
        map.put("categoryList",categoryList);
        /*//3根据分组查询到的商品分类 查询该分类下的品牌和规格
        if (categryList.size()>0){//判断该关键字的item中有没有查到商品分类
            map.putAll(searchBrandAndSpecList(categryList.get(0))); //map.put("brandList",brandList)map.put("specList",specList);
        }*/
        //查询品牌和规格列表
        String categoryName= (String) searchMap.get("category");//获得传过来的那个category分类
        if (!"".equals(categoryName)){//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));//查询这个分类下的品牌和规格 添加到map中
            //{"brandList",List brandList},{"specList",List specList}
        }else {//如果没有分类,按照第一个查询
            if (categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
    }

    /**
     * 更新数据到数据库中
     * @param items 是数据
     */

    public void updateIndex(List<TbItem> items) {
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

    /**
     * 删除数据 更新solr
     * @param goodsIdList
     */

    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品id"+goodsIdList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);//按照solr中的查询条件删除
        solrTemplate.commit();
    }


    /**
     * 查询品牌和规格列表 从redis中拿
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap<>();                   //itemCat是表 从表中根据key取value
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null){// 拿到这个关键词查出来的字段的category name
            //根据模板id查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            //根据模板id查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }
        return map; //{"brandList",List brandList},{"specList",List specList}
    }


    /**
     * 根据关键字查询solr返回一个有高亮的数据
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap<>();
        HighlightQuery query = new SimpleHighlightQuery();//+高亮的查询

        HighlightOptions item_title = new HighlightOptions().addField("item_title");//设置高亮的域
        item_title.setSimplePrefix("<em style='color:red'>");//高亮前缀
        item_title.setSimplePostfix("</em>");//高亮后缀

        query.setHighlightOptions(item_title);//将高亮设置放入query中
        //1.1按照关键字查询 将关键字加入到query中 然后通过solrtemplate去solr中查找
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);//将关键字加载进query中
        //1.2按分类筛选 判断将category:value加入到query中
        String  category = (String) searchMap.get("category");
        if(StringUtils.isNotBlank(category)){  //在item_category表中 查询key(category)的value
            Criteria filerCriteria = new Criteria("item_category").is(category);
            FilterQuery filterQuery = new SimpleFilterQuery(filerCriteria);
            query.addFilterQuery(filterQuery);//将分类筛选的query加入到query中
        }
        //1.3按品牌筛选
        String brand = (String) searchMap.get("brand");
        if(StringUtils.isNotBlank(brand)){
            Criteria filerCriteria = new Criteria("item_brand").is(brand);
            FilterQuery filterQuery = new SimpleFilterQuery(filerCriteria);
            query.addFilterQuery(filterQuery);//将品牌筛选的quary加入到query中
        }
        //1.4按照规格筛选
        Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");//规格是一个map
        if(specMap!=null){
            for (String key : specMap.keySet()) { // key:网络 value:移动3g 条件是item_spec_XXX索引域下的key为?的条件
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);//
                query.addFilterQuery(filterQuery);//将规格的过滤条件条件到高亮条件中
            }
        }
        //1.5按照价格添加筛选添加
        String price = (String) searchMap.get("price");
        if(!"".equals(price) && searchMap.get("price")!=null){
            //item_price:[0 TO 20]
            Criteria priceCriteria = new Criteria("item_price");//指定过滤条件的字段
            String[] split = price.split("-");//按照-分割
            if (!split[1].equals("*")){ //如果判断分割到* 表示查询有上限
                priceCriteria.between(split[0],split[1],true,true);
            }else {//表示无上限
                priceCriteria.greaterThanEqual(split[0]);
            }
            FilterQuery filterQuery = new SimpleFilterQuery(priceCriteria);//将条件放入查询
            query.addFilterQuery(filterQuery);//将价格的过滤条件条件到高亮条件中

        }
        //1.6分页查询 自定义分页不用自带的了 从页面带当前页和每页条数过来
        Integer pageNo = (Integer) searchMap.get("pageNo");

        if (pageNo==0){
            pageNo=1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
        if (pageSize==null){
            pageSize=20;//默认20
        }
        query.setOffset((pageNo-1)*pageSize);//从第几条记录查询
        query.setRows(pageSize);

        //1.7排序
        String sortField = (String) searchMap.get("sortField");//域的名称 去掉item_
        String sortType = (String) searchMap.get("sortType");//desc
        if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortType)){
            //排序
            //item_price asc
            if (sortType.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);//排序
            }else{
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }
//处理高亮分页查询
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();//获得高亮入口集合
        for (HighlightEntry<TbItem> h : highlighted) {
            TbItem item = h.getEntity();//获得原实体类
            List<HighlightEntry.Highlight> highlights = h.getHighlights();
            for (HighlightEntry.Highlight highlight : highlights) {
                List<String> snipplets = highlight.getSnipplets();
                for (String snipplet : snipplets) {
                    item.setTitle(snipplet);//将高亮的元素封装到item的titlezh
                }
            }
        }
        List<TbItem> content = page.getContent();
        map.put("rows",content);
        map.put("total",page.getTotalElements());//获得高亮结果集的全部元素
        map.put("totalPages",page.getTotalPages());//高亮结果集的totalpages

        return map;//{key:rows,value:当前页的高亮数据List<TbItem>}
    }

    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    //select category from tb_item where title like '%手机%' group by category;
    private List searchCategoryList(Map searchMap){//searchMap{{keywords:三星},{item_category:笔记本}}
        List<String> categoryList = new ArrayList<>();
        String keywords = (String) searchMap.get("keywords");//手机

        //select * from tb_item where title like '%手机%'
        Query query = new SimpleQuery("*:*");
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);//将查询条件封装到query集合中

        //分组查询 条件分组条件
        GroupOptions groupOptions = new GroupOptions();
        //group by category
        groupOptions.addGroupByField("item_category");

        query.setGroupOptions(groupOptions);//将查询条件

        //执行分组查询
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分页结果集
        GroupResult<TbItem> item_category = page.getGroupResult("item_category");//指定获取那一个分组域的值
        Page<GroupEntry<TbItem>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();//手机 -》10条item 笔记本 -》20条
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            String groupValue = tbItemGroupEntry.getGroupValue();//category的数据
            categoryList.add(groupValue);
        }
        return categoryList; //{手机，笔记本}
    }

}
