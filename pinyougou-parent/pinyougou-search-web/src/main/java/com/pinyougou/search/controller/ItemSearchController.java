package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 在solr索引域中搜索商品
 * */
@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {

    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map searchMap){ //传入
        return itemSearchService.search(searchMap);

    }
}
