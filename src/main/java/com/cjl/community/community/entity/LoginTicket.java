package com.cjl.community.community.entity;

import lombok.Data;

import java.util.Date;
@Data
public class LoginTicket {

    private Integer id;
    private Integer userId;
    private String ticket;
    private Integer status;
    private Date expired;


}
