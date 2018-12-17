package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbAddressExample;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.service.BrandService;
import entity.PageResult;
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

    /**
     * 分页查询 修改成要条件查询 模糊查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(int pageNum, int pageSize) {
        //用到了pagehelper
        PageHelper.startPage(pageNum,pageSize);
        //需要分页的语句
        Page<TbBrand> page = (Page<TbBrand>) tbBrandMapper.selectByExample(null);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }


    @Override
    public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
        //用到了pagehelper
        PageHelper.startPage(pageNum,pageSize);
        //new的一个pojo类
        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();

        if (brand!=null){
            if (brand.getName()!=null && brand.getFirstChar().length()>0){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if (brand.getFirstChar()!=null&&brand.getFirstChar().length()>0){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
        //需要分页的语句
        Page<TbBrand> page = (Page<TbBrand>) tbBrandMapper.selectByExample(null);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }


    /**
     * 添加评判的实现类在service
     * @param brand
     */
    @Override
    public void add(TbBrand brand) {
        //调用逆向工程中dao 的insert(brand)方法 表示添加brand到breand表中
        tbBrandMapper.insert(brand);
    }

    /**
     * 跟新品牌
     * @param brand
     */
    @Override
    public void update(TbBrand brand) {
        tbBrandMapper.updateByPrimaryKey(brand);
    }

    /**
     * 根据id获得实体
     * @param id
     * @return
     */
    @Override
    public TbBrand findOne(Long id) {
        return tbBrandMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据id数组遍历删除品牌
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        //遍历数组
        for (Long id : ids) {
            tbBrandMapper.deleteByPrimaryKey(id);
        }
    }
}
