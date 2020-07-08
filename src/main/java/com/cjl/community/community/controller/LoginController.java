package com.cjl.community.community.controller;

import com.cjl.community.community.config.KaptchaConfig;
import com.cjl.community.community.entity.User;
import com.cjl.community.community.service.UserService;
import com.cjl.community.community.util.CommunityConstant;
import com.cjl.community.community.util.CommunityUtil;
import com.cjl.community.community.util.CookieUtil;
import com.cjl.community.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author cjl
 * @date 2020/4/10 14:31
 */
@Controller
@Slf4j
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/do_register",method = RequestMethod.POST)
    public String doRegister(Model model,User user){
        Map<String, Object> map = userService.register(user);
        if(map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功,我们已经向您注册的邮箱发送了一封激活邮件，请查看激活");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 登录方法
     * @param model
     * @param username
     * @param password
     * @param code 验证码
     * @return
     */
    @RequestMapping(path = "/do_login",method = RequestMethod.POST)
    public String doLogin(Model model, String username, String password,
                          String code, boolean rememberMe, HttpServletResponse response, HttpServletRequest request,
                          @CookieValue("kaptchaOwner") String kaptchaOwner){
        //判断验证码
        //从session获取验证码与用户提交的验证码判断
        //String kaptcha= (String) session.getAttribute("kaptcha");

        //验证码生成的时候，生成了一个kaptchaOwner给浏览器，从cookie里获取这个kaptchaOwner
        //在此处生成redis的key，从redis查出保存的验证码和户提交的验证码判断
        String kaptcha=null;
        if(StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
             kaptcha= redisTemplate.opsForValue().get(redisKey).toString();
        }
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确!");
            return "/site/login";
        }
        //检查账号密码
        //设置过期时间
        int expiredSeconds=rememberMe ? REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;

        //判断是否有ticket
        String ticket= CookieUtil.getValue(request,"ticket");
        Map<String, Object> map = userService.login(username, password, expiredSeconds,ticket);
        if(map.containsKey("ticket")){
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath("/");
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }

    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logoutByTicket(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }



    //激活账号
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model,@PathVariable("userId") Integer userId,@PathVariable("code")String code){
        int activation = userService.activation(userId, code);
        if(activation==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,您的账号已经可以正常使用了");
            model.addAttribute("target","/login");
        }else if(activation==ACTIVATION_REPEAT){
            model.addAttribute("msg","该账号已经激活，请勿重复激活");
            model.addAttribute("target","/");
        }else {
            model.addAttribute("msg","激活失败，您的激活码不正确");
            model.addAttribute("target","/");
        }
        return "/site/operate-result";

    }
    //获取验证码
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //将验证码存入session
        //session.setAttribute("kaptcha",text);
        //验证码对应用户的凭证
        String kaptchaOwner= CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("/");
        response.addCookie(cookie);
        //将验证码存入redis
        String redisKey= RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            log.error("验证码获取失败："+e.getMessage());
        }

    }

}
