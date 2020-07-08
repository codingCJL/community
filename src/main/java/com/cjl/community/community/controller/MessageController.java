package com.cjl.community.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.cjl.community.community.entity.Message;
import com.cjl.community.community.entity.Page;
import com.cjl.community.community.entity.User;
import com.cjl.community.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author cjl
 * @date 2020/4/14 17:13
 */
@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user=hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.selectConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.selectConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations=new ArrayList<>();
        if(conversationList!=null){
            for(Message message:conversationList){
                Map<String,Object> map=new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.selectLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.selectLetterUnread(user.getId(),message.getConversationId()));
                int targetId=user.getId()==message.getFromId()?message.getToId():message.getFromId();
                map.put("targetUser",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //查询所有的与用户的未读消息
        int letterUnreadCount=messageService.selectLetterUnread(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount=messageService.selectNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.selectLetterCount(conversationId));

        //消息列表
        List<Message> letterList = messageService.selectLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters=new ArrayList<>();
        if(letterList!=null){
            for(Message message:letterList){
                Map<String,Object> map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        //给我发送私信的用户信息
        model.addAttribute("target",getLetterTarget(conversationId));

        //把未读消息设置为已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    //系统通知列表
    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        //查询评论类通知
        Message messageComment=messageService.selectLatestNotice(user.getId(),TOPIC_COMMENT);
        Map<String,Object> messageVoComment=new HashMap<>();
        if(messageComment!=null){
            messageVoComment.put("message",messageComment);
            String content= HtmlUtils.htmlUnescape(messageComment.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVoComment.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVoComment.put("entityType",data.get("entityType"));
            messageVoComment.put("entityId",data.get("entityId"));
            messageVoComment.put("postId",data.get("postId"));
            int count=messageService.selectNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVoComment.put("count",count);
            int unread=messageService.selectNoticeUnreadCount(user.getId(),TOPIC_COMMENT);
            messageVoComment.put("unread",unread);

        }
        model.addAttribute("commentNotice",messageVoComment);

        //查询点赞类通知
        Message messageLike=messageService.selectLatestNotice(user.getId(),TOPIC_LIKE);
        Map<String,Object> messageVoLike=new HashMap<>();
        if(messageLike!=null){
            messageVoLike.put("message",messageLike);
            String content= HtmlUtils.htmlUnescape(messageLike.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVoLike.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVoLike.put("entityType",data.get("entityType"));
            messageVoLike.put("entityId",data.get("entityId"));
            messageVoLike.put("postId",data.get("postId"));
            int count=messageService.selectNoticeCount(user.getId(),TOPIC_LIKE);
            messageVoLike.put("count",count);
            int unread=messageService.selectNoticeUnreadCount(user.getId(),TOPIC_LIKE);
            messageVoLike.put("unread",unread);

        }
        model.addAttribute("likeNotice",messageVoLike);
        //查询关注类通知
        Message messageFollow=messageService.selectLatestNotice(user.getId(),TOPIC_FOLLOW);
        Map<String,Object> messageVoFollow=new HashMap<>();
        if(messageFollow!=null){
            messageVoFollow.put("message",messageFollow);
            String content= HtmlUtils.htmlUnescape(messageFollow.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVoFollow.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVoFollow.put("entityType",data.get("entityType"));
            messageVoFollow.put("entityId",data.get("entityId"));
            int count=messageService.selectNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVoFollow.put("count",count);
            int unread=messageService.selectNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);
            messageVoFollow.put("unread",unread);

        }
        model.addAttribute("followNotice",messageVoFollow);
        //查询未读消息数量
        int letterUnreadCount=messageService.selectLetterUnread(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount=messageService.selectNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/notice";
    }

    //系统通知详情
    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,Page page,Model model){
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);
        page.setRows(messageService.selectNoticeCount(user.getId(),topic));

        List<Message> noticeList=messageService.selectNotices(user.getId(),topic,page.getOffset(),page.getLimit());
        List<Map<String,Object>> noticeVoList=new ArrayList<>();
        if(noticeList!=null){
            for(Message notice:noticeList){
                Map<String,Object> map=new HashMap<>();
                map.put("notice",notice);
                String content=HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //系统用户
                map.put("fromUser",userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);
        //把未读消息设置为已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }

    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        Message message=new Message();
        User target = userService.findUserByUsername(toName);
        if(target==null){
            return CommunityUtil.getJSONString(1,"用户不存在！");
        }
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else {
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }



    private User getLetterTarget(String conversationId ){
        String[] ids = conversationId.split("_");
        int id0=Integer.parseInt(ids[0]);
        int id1=Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId()==id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }
    }

    //获取未读消息id集合
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids=new ArrayList<>();
        if(letterList!=null){
            for(Message message:letterList){
                if(hostHolder.getUser().getId().equals(message.getToId())  && message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }
}
