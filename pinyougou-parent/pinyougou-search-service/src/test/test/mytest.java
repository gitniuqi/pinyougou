package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-solr.xml")
public class mytest {

    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void testAdd(){
        TbItem tbItem = new TbItem();
        tbItem.setId(1L);
        tbItem.setBrand("apple");
        tbItem.setCategory("手机");
        tbItem.setGoodsId(1L);
        tbItem.setSeller("apple旗舰店");
        tbItem.setTitle("applex");
        tbItem.setPrice(new BigDecimal(20000));
        solrTemplate.saveBean(tbItem);
        solrTemplate.commit();
    }
    //按主键进行查询
    @Test
    public void testFindOne(){
        TbItem byId = solrTemplate.getById(1, TbItem.class);
        System.out.println(byId);
    }
    //按主键删除整个文本域
    @Test
    public void testDelete(){
        solrTemplate.deleteById("1");//byid删除
        solrTemplate.commit();
    }

    @Test
    public void testAddList(){
        List<TbItem> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TbItem tbItem = new TbItem();
            tbItem.setId(i+1L);
            tbItem.setBrand("apple");
            tbItem.setCategory("手机");
            tbItem.setGoodsId(1L);
            tbItem.setSeller("apple官网");
            tbItem.setTitle("applex"+i);
            tbItem.setPrice(new BigDecimal(20000+i));
            list.add(tbItem);
        }
        solrTemplate.saveBeans(list);//将list集合封装到solr文本域中 lucene
        solrTemplate.commit();
    }
    //分页查询
    @Test
    public void testPageQuery(){
        SimpleQuery simpleQuery = new SimpleQuery("*:*");
        simpleQuery.setOffset(20);//开始分页的索引
        simpleQuery.setRows(20);//每页记录数
        ScoredPage<TbItem> page = solrTemplate.queryForPage(simpleQuery, TbItem.class);
        System.out.println("总记录数:"+page.getTotalElements());
        List<TbItem> content = page.getContent();//获得分页集合
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle()+tbItem.getPrice());//title+价格
        }
    }
    //条件查询
    @Test
    public void testPageQueryMutil(){
        Query simpleQuery = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_title").contains("2");
        criteria=criteria.and("item_title").contains("5");
        simpleQuery.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(simpleQuery, TbItem.class);
        System.out.println("总记录数："+page.getTotalElements());
        List<TbItem> content = page.getContent();
        for (TbItem tbItem : content) {
            System.out.println(tbItem.getTitle()+":"+tbItem.getPrice());
        }
    }
    //批量删除文本域 全部
    @Test
    public void testDeleteAll(){
        Query simpleQuery = new SimpleQuery("*:*");
        solrTemplate.delete(simpleQuery);
        solrTemplate.commit();
    }

}
