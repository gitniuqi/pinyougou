package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

/**
 * set数据类型set和list是cont...set内元素不能重复 自动排序
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class TestSet {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 存入值
     */
    @Test
    public void setValue(){
        redisTemplate.boundSetOps("nameset").add("曹操");
        redisTemplate.boundSetOps("nameset").add("刘备");
        redisTemplate.boundSetOps("nameset").add("孙权");
    }

    @Test
    public void getValue(){
        Set nameset = redisTemplate.boundSetOps("nameset").members();
        System.out.println(nameset);
    }

    /**
     * 删除 移除
     */
    @Test
    public void deleteValue(){
        redisTemplate.boundSetOps("nameset").remove("孙权");
    }

    /**
     * 删除整个
     */
    @Test
    public void deleteAllValue(){
        redisTemplate.delete("nameset");
    }


}
