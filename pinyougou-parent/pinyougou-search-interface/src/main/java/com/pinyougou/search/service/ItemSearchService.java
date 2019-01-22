package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 更新数据到索引库中 为什么写在这里因为这里有配置solr
     * @param items 是数据
     */
    public void updateIndex(List<TbItem> items);

    /**
     * 删除数据
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);



}
