package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import java.util.List;
import java.util.Map;

public class SolrUtil {

    @Autowired
    private TbItemMapper tbItemMapper; //

    @Autowired
    private SolrTemplate solrTemplate;//

    /**
     * 将商品数据导入到索引库中
     */
    public void importItemData(){
        //先通过条件查询到已经审核的商品
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = tbItemMapper.selectByExample(example);

        //索引库中留有动态域 但是数据库 pojo没有封装或者说给的是一个不好的字符串{"机身内存":"16G","网络":"移动2G"}
        //要拿出元素 并赋值到索引库中动态生成id keyxxx:数据
        for (TbItem item : itemList) {
            Map map = JSON.parseObject(item.getSpec(), Map.class);//将json转换成map("机身内存","16G")
            item.setSpecMap(map);//将由数据库动态生成的数据 封装到pojo中


        }

        solrTemplate.saveBeans(itemList);//将所有商品导入索引库
        solrTemplate.commit();
    }



}
