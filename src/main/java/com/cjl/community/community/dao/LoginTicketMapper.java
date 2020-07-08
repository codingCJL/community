package com.cjl.community.community.dao;

import com.cjl.community.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

import java.util.Date;

@Mapper
public interface LoginTicketMapper {

    @Insert("insert into login_ticket (user_id,ticket,status,expired) " +
            "values(#{userId},#{ticket},#{status},#{expired})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select("select * from login_ticket where ticket=#{ticket}")
    LoginTicket selectByTicket(String ticket);

    @Update("update login_ticket set status=#{status} where ticket=#{ticket}")
    int updateStatusByTicket(String ticket,Integer status);

    @Update("update login_ticket set status=#{status} where user_id=#{userId}")
    int updateStatusByUserId(Integer userId,Integer status);

    @Update("update login_ticket set expired=#{expired} where ticket=#{ticket}")
    int updateExpired(String ticket,Date expired);
}
