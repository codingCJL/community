package com.cjl.community.community.controller;

import com.cjl.community.community.annotation.LoginRequired;
import com.cjl.community.community.entity.Comment;
import com.cjl.community.community.entity.DiscussPost;
import com.cjl.community.community.entity.Event;
import com.cjl.community.community.event.EventConsumer;
import com.cjl.community.community.event.EventProducer;
import com.cjl.community.community.service.CommentService;
import com.cjl.community.community.service.DiscussPostService;
import com.cjl.community.community.util.CommunityConstant;
import com.cjl.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.List;

/**
 * @author cjl
 * @date 2020/4/14 11:08
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;


    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") Integer discussPostId,Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        if(comment.getTargetId()==null){
            comment.setTargetId(0);
        }
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event=new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost post = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(post.getUserId());
        }else if(comment.getEntityType()== ENTITY_TYPE_COMMENT){
            Comment c = commentService.selectCommentById(comment.getEntityId());
            event.setEntityUserId(c.getUserId());
        }
        eventProducer.fireEvent(event);

        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //以Kafka事件方式将帖子提交到es服务器
            Event e=new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(e);
        }
        return "redirect:/discuss/detail/"+discussPostId;
    }

}
