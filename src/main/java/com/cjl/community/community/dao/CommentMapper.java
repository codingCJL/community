package com.cjl.community.community.dao;

import com.cjl.community.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author cjl
 * @date 2020/4/14 11:07
 */
@Mapper
public interface CommentMapper {
    /**
     * 根据id和类型查询所有评论
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 根据id和类型查询所有评论数目
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCountByEntity(int entityType,int entityId);

    /**
     * 增加评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);

    /**
     *  根据id查询评论
     * @param id
     * @return
     */
    Comment selectCommentById(int id);

}
