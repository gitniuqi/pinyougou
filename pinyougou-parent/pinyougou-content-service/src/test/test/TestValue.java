package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * string类型操作
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class TestValue {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void setValue(){
        //将数据添加到redis中
        redisTemplate.boundValueOps("name").set("itcast");
    }
    @Test
    public void getValue(){
        String name = (String) redisTemplate.boundValueOps("name").get();
        System.out.println(name);
    }
    @Test
    public void deleteValue(){
        redisTemplate.delete("name");
    }

}
