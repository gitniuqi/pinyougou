package com.pinyougou;

import com.pinyougou.solr.SolrUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        //1.手动初始化spring容器
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");

        //2.从容器中获取solruitl对象
        SolrUtil bean = context.getBean(SolrUtil.class);
        //3.调用导入的方法
        bean.importItemData();
    }
}
