package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-redis.xml")
public class TestList {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 右压栈：后添加的对象排在后边
     */
    @Test
    public void testSetValuer1(){
        redisTemplate.boundListOps("namelist1").rightPush("刘备");
        redisTemplate.boundListOps("namelist1").rightPush("关羽");
        redisTemplate.boundListOps("namelist1").rightPush("张飞");
    }

    /**
     * 显示右压栈集合
     */
    @Test
    public void testGetValuer1(){
        List namelist1 = redisTemplate.boundListOps("namelist1").range(0, -1);
        System.out.println(namelist1);
    }
    /**
     * 左压入队列
     */
    @Test
    public void testSetValue2(){
        redisTemplate.boundListOps("namelist2").leftPush("刘备");
        redisTemplate.boundListOps("namelist2").leftPush("关羽");
        redisTemplate.boundListOps("namelist2").leftPush("张飞");
    }

    /**
     * 显示左压栈集合
     */
    @Test
    public void testGetValue2(){
        List list = redisTemplate.boundListOps("namelist2").range(0, -1);
        System.out.println(list);
    }

    /**
     * 从右边弹出
     */
    @Test
    public void testPopValue1(){
        Object item = redisTemplate.boundListOps("namelist2").rightPop();
        System.out.println(item);
    }
    /**
     * 从左边弹出
     */
    @Test
    public void testPopValue2(){
        Object item = redisTemplate.boundListOps("namelist2").leftPop();
        System.out.println(item);
    }


}
