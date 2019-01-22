package test;

import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@ContextConfiguration("classpath:spring/spring-applicationContext-producer.xml")
@RunWith(SpringRunner.class)
public class testSpringSend {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue activeMQQueue;
    @Test
    public void sendMassages(){
        //参数1要邮箱 2一个匿名内部类
        jmsTemplate.send(activeMQQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                //创建一个消息
                return session.createTextMessage("有您的快递");
            }
        });
    }
}
