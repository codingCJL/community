package com.cjl.community.community.controller;

import com.cjl.community.community.entity.*;
import com.cjl.community.community.event.EventProducer;
import com.cjl.community.community.service.CommentService;
import com.cjl.community.community.service.DiscussPostService;
import com.cjl.community.community.service.LikeService;
import com.cjl.community.community.service.UserService;
import com.cjl.community.community.util.CommunityConstant;
import com.cjl.community.community.util.CommunityUtil;
import com.cjl.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author cjl
 * @date 2020/4/4 23:29
 */
@Controller
@RequestMapping("/discuss")
public class DisscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String add(String title,String content){
        User currentUser = hostHolder.getUser();
        if(currentUser==null){
            return CommunityUtil.getJSONString(403,"您还没有登录！");
        }
        DiscussPost post=new DiscussPost();
        post.setUserId(currentUser.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setType(0);
        post.setStatus(0);
        post.setScore(0d);
        post.setCommentCount(0);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        //以Kafka事件方式将帖子提交到es服务器
        Event event=new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(currentUser.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @RequestMapping(path = "/detail/{id}",method = RequestMethod.GET)
    public String detail(Model model, @PathVariable("id") int id,Page page){
        //帖子信息
        DiscussPost discussPost=discussPostService.selectDiscussPostById(id);
        //用户信息
        User user=userService.findUserById(discussPost.getUserId());
        //点赞数量
        long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST,id);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus=hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,id);
        model.addAttribute("likeStatus",likeStatus);
        //评论信息
        page.setLimit(10);
        page.setPath("/discuss/detail/"+id);
        page.setRows(discussPost.getCommentCount());
        List<Comment> commentList = commentService.selectCommentsByEntity(ENTITY_TYPE_POST, id, page.getOffset(), page.getLimit());
        //封装帖子评论和用户信息，回复和用户信息
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                Map<String,Object> commentVo=new HashMap<>();
                //帖子评论
                commentVo.put("comment",comment);
                //帖子评论的用户信息
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                likeStatus=hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);
                //回复评论列表
                List<Comment> replyLists = commentService.selectCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                if(replyLists!=null){
                    for(Comment reply:replyLists){
                        Map<String,Object> replyVo=new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target=reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        //点赞数量
                        likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus=hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replyVoList",replyVoList);
                //回复数量
                int replyCount=commentService.selectCountByEntity(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("discussPost",discussPost);
        model.addAttribute("user",user);
        model.addAttribute("commentVoList",commentVoList);
        return "/site/discuss-detail";
    }

    //置顶
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        if(discussPostService.updateType(id,1)>0){
            //同步到es服务器
            //触发发帖事件
            Event event=new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);
            return CommunityUtil.getJSONString(0,"帖子已置顶！");
        }
        return CommunityUtil.getJSONString(500,"置顶失败！");
    }

    //加精
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        if(discussPostService.updateStatus(id,1)>0){
            //同步到es服务器
            //触发发帖事件
            Event event=new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);
            return CommunityUtil.getJSONString(0,"帖子已加精！");
        }
        return CommunityUtil.getJSONString(500,"加精失败！");
    }

    //删除
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        if(discussPostService.updateStatus(id,2)>0){
            //同步到es服务器
            //触发删帖事件
            Event event=new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);
            return CommunityUtil.getJSONString(0,"删除成功！");
        }
        return CommunityUtil.getJSONString(500,"删除失败！");
    }



}
