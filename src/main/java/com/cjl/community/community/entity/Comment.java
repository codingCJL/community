package com.cjl.community.community.entity;

import lombok.Data;

import java.util.Date;
@Data
public class Comment {

    private Integer id;
    private Integer userId;
    //1是对帖子评论，2是对评论回复
    private Integer entityType;
    private Integer entityId;
    private Integer targetId;
    private String content;
    private Integer status;
    private Date createTime;


}
