package com.cjl.community.community.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjl
 * @date 2020/4/23 10:31
 * 对事件进行封装，如评论、点赞
 */
public class Event {
    //事件类型,对应Kafka的主题
    private String topic;
    //触发人
    private Integer userId;
    //实体类型
    private Integer entityType;
    private Integer entityId;
    private Integer entityUserId;
    //额外数据
    private Map<String,Object> data=new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Integer getUserId() {
        return userId;
    }

    public Event setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public Integer getEntityType() {
        return entityType;
    }

    public Event setEntityType(Integer entityType) {
        this.entityType = entityType;
        return this;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public Event setEntityId(Integer entityId) {
        this.entityId = entityId;
        return this;
    }

    public Integer getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(Integer entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }
}
