package com.cjl.community.community.dao;

import com.cjl.community.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author cjl
 * @date 2020/4/9 16:18
 */
@Mapper
public interface UserMapper{
    @Select("select * from user")
    List<User> findAll();

    User selectById(Integer id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(Integer id,Integer status);

    int updateHeader(Integer id,String headerUrl);

    int updatePassword(Integer id,String password);


}
