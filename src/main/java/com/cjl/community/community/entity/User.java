package com.cjl.community.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author cjl
 * @date 2020/4/9 16:19
 */
@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private Integer type;
    private Integer status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;
}
