package com.beautifulsoup.chengfeng.rabbitmq;

import com.beautifulsoup.chengfeng.constant.ChengfengConstant;
import com.beautifulsoup.chengfeng.dao.AssembleMapper;
import com.beautifulsoup.chengfeng.pojo.Assemble;
import com.beautifulsoup.chengfeng.utils.JsonSerializableUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
public class OrderReceiver {

    @Autowired
    private AssembleMapper assembleMapper;

    @RabbitListener(queues = ChengfengConstant.RabbitMQ.QUEUE_NAME_SPELL_ORDER)
    public void process(String msg,Message message, Channel channel){
        if (!StringUtils.isBlank(msg)){
            try{
                Assemble assemble= JsonSerializableUtil.string2Obj(msg,Assemble.class);
                Assemble dbAssemble = assembleMapper.selectByPrimaryKey(assemble.getId());
                if (dbAssemble.getStatus()==1){
                    dbAssemble.setStatus(0);
                    assembleMapper.updateByPrimaryKeySelective(dbAssemble);
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }finally {
                try {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
