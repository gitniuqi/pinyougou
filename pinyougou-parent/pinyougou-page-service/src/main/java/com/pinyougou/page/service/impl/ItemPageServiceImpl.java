package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Override
    public void genHtml(Long id) {
        //1从数据库中获取数据集(商品的数据)
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        //2.使用freemarker的APi来实现生成静态页面
        genHTMLfromFreemarker(tbGoods,tbGoodsDesc);

    }

    /**
     * 数据集+模板=heml的私有方法
     * @param tbGoods
     * @param tbGoodsDesc
     */
    private void genHTMLfromFreemarker(TbGoods tbGoods, TbGoodsDesc tbGoodsDesc) {
        try {
            //1将在spring配置文件注册好的freemark配置文件拿到
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //2准备模板类
            Template template = configuration.getTemplate("item.ftl");
            //3准备结果集
            Map map = new HashMap();
            map.put("goods",tbGoods); //相当于jsp渲染的request.setAttribute()
            map.put("goodsDesc",tbGoodsDesc);//相对于 request.setAttribute()

            //查询三级分类的名称 在goods中id
            TbItemCat tbItemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            TbItemCat tbItemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            TbItemCat tbItemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());

            map.put("tbItemCat1",tbItemCat1.getName());
            map.put("tbItemCat2",tbItemCat2.getName());
            map.put("tbItemCat3",tbItemCat3.getName());

            //查询spu对应的所有的sku的列表 数据 存储到js中
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(tbGoods.getId());
            criteria.andStatusEqualTo("1");
            example.setOrderByClause("is_default desc");//asc 升序 desc降序
            List<TbItem> itemList = itemMapper.selectByExample(example);

            map.put("skuList",itemList);

            //开始合体
            //输出到文件中(合体后放哪里)
            Writer out = new FileWriter(new File("D:\\freemarkerTest\\"+tbGoods.getId()+".html"));
            //合体
            template.process(map,out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
