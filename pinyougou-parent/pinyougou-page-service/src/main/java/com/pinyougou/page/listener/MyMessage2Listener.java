package com.pinyougou.page.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MyMessage2Listener implements MessageListener{

    @Override
    public void onMessage(Message message) {
        //有消息的时候触发这个方法执行这个逻辑
        if (message instanceof TextMessage){
           TextMessage message1 = (TextMessage) message;
            try {
                //从消息中拿到信息 是用string传送
                String text = message1.getText();
                System.out.println("1>>>>"+text);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
