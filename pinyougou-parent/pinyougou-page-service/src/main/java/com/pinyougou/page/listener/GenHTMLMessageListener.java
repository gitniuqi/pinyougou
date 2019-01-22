package com.pinyougou.page.listener;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

public class GenHTMLMessageListener implements MessageListener{

    @Autowired
    private ItemPageService pageService;
    @Override
    public void onMessage(Message message) {
        if(message instanceof ObjectMessage){
            try {
                //1.接收消息  long[]
                //2.转成Long[]
                //3.循环遍历
                //4.查询数据库中的商品SKU数据
                //5.生成静态页面

                ObjectMessage objectMessage =(ObjectMessage)message;
                Serializable object = objectMessage.getObject();

                Long[] ids  = (Long[]) object;

                for (Long id : ids) {
                    pageService.genHtml(id);
                }

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }


}
