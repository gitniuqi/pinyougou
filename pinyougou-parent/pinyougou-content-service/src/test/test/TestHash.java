package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-redis.xml")
public class TestHash {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testSetValue(){
        redisTemplate.boundHashOps("namehash").put("a", "唐僧");
        redisTemplate.boundHashOps("namehash").put("b", "悟空");
        redisTemplate.boundHashOps("namehash").put("c", "八戒");
        redisTemplate.boundHashOps("namehash").put("d", "沙僧");
    }
    /**
     * 获取所有的key
     */
    @Test
    public void testGetKeys(){
        Set s = redisTemplate.boundHashOps("namehash").keys();
        System.out.println(s);
    }

    /**
     * 获取所有的值
     */
    @Test
    public void testGetValues(){
        List values = redisTemplate.boundHashOps("namehash").values();
        System.out.println(values);
    }

    /**
     * 根据key 获取值
     */
    @Test
    public void testGetValueByKey(){
        Object object = redisTemplate.boundHashOps("namehash").get("b");
        Object a = redisTemplate.boundHashOps("TB_CONTENT_REDIS_INDEX_KEY").get(1);

        System.out.println(a);
    }

    /**
     * 根据key 删除值
     */
    @Test
    public void testRemoveValueByKey(){
        redisTemplate.boundHashOps("namehash").delete("c");

    }

}
