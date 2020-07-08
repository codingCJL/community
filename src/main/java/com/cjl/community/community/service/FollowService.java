package com.cjl.community.community.service;

import com.cjl.community.community.entity.User;
import com.cjl.community.community.util.CommunityConstant;
import com.cjl.community.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //关注，需要两步操作，添加关注key和被关注key
    //表示userId用户关注了类型为entityType的entityId，存入zset，score为now
    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey= RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey= RedisKeyUtil.getFollowerKey(entityType,entityId);
                operations.multi();
                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                return operations.exec();
            }
        });
    }
    //取消关注
    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey= RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey= RedisKeyUtil.getFollowerKey(entityType,entityId);
                operations.multi();
                operations.opsForZSet().remove(followeeKey,entityId);
                operations.opsForZSet().remove(followerKey,userId);
                return operations.exec();
            }
        });
    }

    //查询关注的实体的数量(某人关注了多少人)
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey= RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }
    //查询实体的粉丝数量(某人有多少粉丝)
    public long findFollowerCount(int entityType,int entityId){
        String followerKey= RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }
    //当前用户是否关注某个实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey= RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }

    //查询某个用户关注的人
    public List<Map<String,Object>> findFollowees(int userId,int offset,int limit){
        String followeeKey= RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        Set<Integer> followeeIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if(followeeIds==null){
            return null;
        }
       List<Map<String,Object>> list=new ArrayList<>();
        for(Integer followeeId:followeeIds){
            Map<String,Object> map=new HashMap<>();
            User user=userService.findUserById(followeeId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, followeeId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
    //查询某个用户的粉丝
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey= RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if(followerIds==null){
            return null;
        }
        List<Map<String,Object>> list=new ArrayList<>();
        for(Integer followerId:followerIds){
            Map<String,Object> map=new HashMap<>();
            User user=userService.findUserById(followerId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, followerId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
