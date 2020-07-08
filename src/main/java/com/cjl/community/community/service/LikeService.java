package com.cjl.community.community.service;

import com.cjl.community.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞方法,把点赞的用户id存入set里
     * 由于需要进行两次更新操作，需要保证事务性
     * @param userId 谁点赞
     * @param entityType 点赞的类型（帖子或者评论）
     * @param entityId 点赞的实体id
     * @param entityUserId 被点赞的用户id
     */
    public void like(int userId,int entityType,int entityId,int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey= RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean hasLike = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();
                if(hasLike){
                    //取消点赞
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    //记录被点赞的用户的点赞数量，即-1
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else {
                    //点赞
                    redisOperations.opsForSet().add(entityLikeKey,userId);
                    //记录被点赞的用户的点赞数量，即+1
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询某个实体被点赞的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType,int entityId){
        String likeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(likeKey);
    }

    /**
     * 查询某人对某实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String likeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(likeKey,userId)?1:0;
    }

    /**
     * 查询询某个用户帖子或评论被点赞的数量
     * @param userId
     * @return
     */
    public int findUserLikeCount(int userId){
        String userLikeKey=RedisKeyUtil.getUserLikeKey(userId);
        Integer count=(Integer)redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count.intValue();
    }
}
