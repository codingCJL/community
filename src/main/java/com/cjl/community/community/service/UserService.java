package com.cjl.community.community.service;

import com.cjl.community.community.dao.LoginTicketMapper;
import com.cjl.community.community.dao.UserMapper;
import com.cjl.community.community.entity.LoginTicket;
import com.cjl.community.community.entity.User;
import com.cjl.community.community.util.CommunityConstant;
import com.cjl.community.community.util.CommunityUtil;
import com.cjl.community.community.util.MailClient;
import com.cjl.community.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author cjl
 * @date 2020/4/9 16:37
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    //redis缓存处理
    public User findUserById(Integer userId){
        User user = getCache(userId);
        if(user==null){
             user = initCache(userId);
        }
        return user;
    }
    public User findUserByUsername(String username){
        return userMapper.selectByName(username);
    }
    public List<User> findAll(){
        return userMapper.findAll();
    }


    public Map<String,Object> register(User user){
        Map<String,Object> map=new HashMap<>();
        //参数校验
        if(user==null){
            throw new IllegalArgumentException("参数user不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","账号已存在！");
            return map;
        }
        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","邮箱已被使用！");
            return map;
        }
        //注册
        //加密
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        String password=CommunityUtil.MD5(user.getPassword()+user.getSalt());
        user.setPassword(password);
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl("https://i.picsum.photos/id/"+ new Random().nextInt(999) +"/200/200.jpg");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        String url=domain+contextPath+"activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content=templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"社区讨论网-激活账号",content);
        //map.put("userMsg","注册成功！");
        return map;
    }

    public int activation(Integer userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            //重复激活
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            //可以激活
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAIL;
        }

    }

    /**
     * 登录方法
     * @param username
     * @param password
     * @param expiredSeconds ticket多长时间过期
     * @return
     */
    public Map<String,Object> login(String username,String password,int expiredSeconds,String ticket){
        Map<String,Object> map=new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        //验证账号
        User user=userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","账号不存在！");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","用户还没有激活！");
            return map;
        }
        //验证密码
        password=CommunityUtil.MD5(password+user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        //0表示有效
        loginTicket.setStatus(0);
        //过期时间
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        //loginTicketMapper.insertLoginTicket(loginTicket);
        //将登录凭证存入redis
        String redisKey= RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登录
     * @param ticket
     */
    public void logoutByTicket(String ticket){
        String redisKey= RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        //表示删除态
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    public void logoutByUserId(Integer userId) {
        loginTicketMapper.updateStatusByUserId(userId,1);
    }

    /**
     * 查询凭证
     * @param ticket
     * @return
     */
    public LoginTicket getLoginTicket(String ticket){
        String redisKey= RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        return loginTicket;
    }

    /**
     * 更新头像
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(Integer userId,String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    /**
     * 更新密码
     * @param userId
     * @param newPassword
     * @return
     */
    public int updatePassword(Integer userId,String newPassword){
        return userMapper.updatePassword(userId,newPassword);
    }


    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    //2.取不到时初始化缓存数据，取到直接返回
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //3.数据变更时清楚缓存数据
    private void clearCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    //获得用户的权限
    public Collection<? extends GrantedAuthority> getAuthority(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list=new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                //根据用户表的type字段返回权限
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
