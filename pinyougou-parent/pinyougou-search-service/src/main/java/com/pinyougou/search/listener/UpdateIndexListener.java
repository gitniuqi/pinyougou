package com.pinyougou.search.listener;


import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

public class UpdateIndexListener implements MessageListener{

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage){
           TextMessage message1 = (TextMessage) message;
            try {
                String text = message1.getText();
                //将String转对象
                List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
                itemSearchService.updateIndex(itemList);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
