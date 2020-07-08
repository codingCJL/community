package com.cjl.community.community.dao;

import com.cjl.community.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author cjl
 * @date 2020/4/9 17:51
 */
@Mapper
public interface DiscussPostMapper {
    /**
     * 查询帖子
     * @param userId 用户id
     * @param offset 当前页数
     * @param limit  查询多少条
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    /**
     * 查询所有帖子
     * @return
     */
    List<DiscussPost> selectDiscussAllPosts();


    /**
     * @Param注解用于给参数取别名
     * 如果只有一个参数，并且在<if>里使用，则必须加别名
     * 查询总共多少条帖子
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 增加一条帖子
     * @param discussPost
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 查询帖子详情
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更改评论数量
     * @param id
     * @param commentCount
     * @return
     */
    int updateCommentCount(int id,int commentCount);

    /**
     * 修改类型
     * @param id
     * @param type 0-普通; 1-置顶
     * @return
     */
    int updateType(int id,int type);

    /**
     * 修改状态
     * @param id
     * @param status 0-正常; 1-精华; 2-拉黑;
     * @return
     */
    int updateStatus(int id,int status);
}
