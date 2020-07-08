package com.cjl.community.community.service;

import com.cjl.community.community.dao.MessageMapper;
import com.cjl.community.community.entity.Message;
import com.cjl.community.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author cjl
 * @date 2020/4/14 17:34
 */
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;
    /**
     * 查询当前用户的会话列表
     * 针对每个会话只返回一条最新的私信
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> selectConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    /**
     * 查询当前用户的会话数量
     * @param userId
     * @return
     */
    public int selectConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }


    /**
     * 查询某个会话所包含的所有私信
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> selectLetters(String conversationId,int offset,int limit){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId
     * @return
     */
    public int selectLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }


    /**
     * 查询对话的未读数量，包括所有的未读，和某个会话的未读
     * @param userId
     * @param conversationId
     * @return
     */
    public int selectLetterUnread(int userId,String conversationId){
        return messageMapper.selectLetterUnread(userId,conversationId);
    }

    /**、
     * 添加消息
     * @param message
     * @return
     */
    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 更改消息设置为已读
     * @param ids
     * @return
     */
    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

    /**
     * 查询某个主题下最新的通知
     * @param userId
     * @param topic
     * @return
     */
    public Message selectLatestNotice(int userId,String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }

    /**
     * 查询某个主题所有的通知数量
     * @param userId
     * @param topic
     * @return
     */
    public int selectNoticeCount(int userId,String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }

    /**
     * 查询某个主题的未读的通知数量
     * @param userId
     * @param topic
     * @return
     */
    public int selectNoticeUnreadCount(int userId,String topic){
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    /**
     * 查询某个主题下的通知列表
     * @param userId
     * @param topic
     * @return
     */
    public List<Message> selectNotices(int userId,String topic,int offset,int limit){
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
