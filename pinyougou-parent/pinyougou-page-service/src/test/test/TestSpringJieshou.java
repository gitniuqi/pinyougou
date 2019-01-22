package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@ContextConfiguration("classpath:spring/applicationContext-consumer.xml")
@RunWith(SpringRunner.class)
public class TestSpringJieshou {

    @Test
    public void jiesho(){
        System.out.println("这是什么？？");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
