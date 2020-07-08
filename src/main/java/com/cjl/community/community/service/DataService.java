package com.cjl.community.community.service;

import com.cjl.community.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd");

    //将指定的ip计入uv
    public void recordUV(String ip){
        String redisKey=RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }
    //统计指定日期范围内指定的uv
    public long calculateUV(Date start,Date end){
        if(start==null||end==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //整理该日期范围内的key
        List<String> keyList=new ArrayList<>();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        //合并这段时间的数据
        String redisKey=RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());
        //返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);

    }

    //将指定的用户计入dav
    public void recordDAU(int userId){
        String redisKey=RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }
    //统计指定日期范围内指定的DAU
    public long calculateDAU(Date start,Date end){
        if(start==null||end==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //整理该日期范围内的key
        List<byte[]> keyList=new ArrayList<>();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        //进行or运算
        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });

    }
}