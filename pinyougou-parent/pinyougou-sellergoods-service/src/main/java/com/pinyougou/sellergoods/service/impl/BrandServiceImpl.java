package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service //alibaba
public class BrandServiceImpl implements BrandService {
    //逆向工程 生成TbBrandExample
    @Autowired
    private TbBrandMapper tbBrandMapper;
    @Override
    public List<TbBrand> findAll() {
        // 查询通过例子参数为空 表示查询所有
        List<TbBrand> tbBrands = tbBrandMapper.selectByExample(null);
        return  tbBrands;
    }
}
