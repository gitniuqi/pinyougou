package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 */
@RestController  //@responseBody + @controller 以后这个类中的方法的返回值都是json类型
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     * 查询所有
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findll(){
        return brandService.findAll();
    }

    /**
     * 利用pagehelper分页查询
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page,int rows){
        return brandService.findPage(page,rows);
    }

    /**
     * 查询pagehelper分页
     * @param brand
     * @param page
     * @param rows
     * @return
     */
   /* @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand,int page, int rows){
        return brandService.findPage(brand,page,rows);
    }
*/

    /**
     * 添加品牌到数据库
     * @param brand 页码传过来的brand信息
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    /**
     * 跟新品牌 先查询再保存
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){

        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 通过id查询品牌
     * @return
     */
    //注意这里不能加@RequestBody 如果是用json来传输的绝要用 不是就不用 get 和 post
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        TbBrand brand = brandService.findOne(id);
        return brand;
    }

    /**
     * 根据id结合删除品牌
     * @param ids 是要删除的集合id
     * @return 一个固定封装的结果类
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            //掉用service删除
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
}
