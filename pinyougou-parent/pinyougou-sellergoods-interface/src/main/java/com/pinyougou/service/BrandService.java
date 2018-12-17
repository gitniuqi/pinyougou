package com.pinyougou.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

/**
 * 这是接口系统的品牌接口
 */
public interface BrandService {

    /**
     * 查询所有
     * @return
     */
    List<TbBrand> findAll();

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */

    PageResult findPage(int pageNum,int pageSize);

    PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    /**
     * 添加品牌
     * @param brand
     */
    void add(TbBrand brand);

    /**
     * 跟新品牌
     * @param brand
     */
    void update(TbBrand brand);

    /**
     * 根据id查询到一个品牌实体
     * @param id
     * @return
     */
    TbBrand findOne(Long id);

    /**
     * 根据id数组批量删除
     * @param ids
     */
    void delete(Long[] ids);
}
