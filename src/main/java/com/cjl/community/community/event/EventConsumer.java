package com.cjl.community.community.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjl.community.community.entity.DiscussPost;
import com.cjl.community.community.entity.Event;
import com.cjl.community.community.entity.Message;
import com.cjl.community.community.service.DiscussPostService;
import com.cjl.community.community.service.ESearchService;
import com.cjl.community.community.service.MessageService;
import com.cjl.community.community.util.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjl
 * @date 2020/4/23 10:40
 */
@Component
@Slf4j
public class EventConsumer implements CommunityConstant {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ESearchService eSearchService;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handlerCommentMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            log.error("消息内容为空！");
            return;
        }
        Event event= JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            log.error("消息格式错误！");
            return;
        }
        //发送站内通知
        Message message=new Message();
        //表示系统发送
        message.setFromId(SYSTEM_USER_ID);
        //消息接收者
        message.setToId(event.getEntityUserId());
        //主题
        message.setConversationId(event.getTopic());
        message.setStatus(0);
        message.setCreateTime(new Date());
        //消息内容
        Map<String,Object> content=new HashMap<>();
        //触发者
        content.put("userId",event.getUserId());
        //触发类型
        content.put("entityType",event.getEntityType());
        //触发类型id
        content.put("entityId",event.getEntityId());
        if(!event.getData().isEmpty()){
            for (Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //消费发帖事件（新增或增加评论时将事件生产到Kafka，消费到事件后将对应的帖子放入es服务器）

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlerPublishPost(ConsumerRecord record){
        if(record==null||record.value()==null){
            log.error("消息内容为空！");
            return;
        }
        Event event= JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            log.error("消息格式错误！");
            return;
        }
        DiscussPost post = discussPostService.selectDiscussPostById(event.getEntityId());
        eSearchService.saveDiscussPost(post);
    }
    //消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handlerDeletePost(ConsumerRecord record){
        if(record==null||record.value()==null){
            log.error("消息内容为空！");
            return;
        }
        Event event= JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            log.error("消息格式错误！");
            return;
        }
        eSearchService.deleteDiscussPost(event.getEntityId());
    }
}
