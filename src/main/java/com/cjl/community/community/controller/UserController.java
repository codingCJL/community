package com.cjl.community.community.controller;

import com.cjl.community.community.annotation.LoginRequired;
import com.cjl.community.community.dao.LoginTicketMapper;
import com.cjl.community.community.entity.User;
import com.cjl.community.community.service.FollowService;
import com.cjl.community.community.service.LikeService;
import com.cjl.community.community.service.UserService;
import com.cjl.community.community.util.CommunityConstant;
import com.cjl.community.community.util.CommunityUtil;
import com.cjl.community.community.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author cjl
 * @date 2020/4/4 23:29
 */
@Controller
@Slf4j
public class UserController implements CommunityConstant {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;
    

    @RequestMapping(path = "/user/setting",method = RequestMethod.GET)
    public String settingPage(){
        return "/site/setting";
    }

    @RequestMapping(path = "/user/uploadHeader",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImageFile, Model model){
        if(headerImageFile==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        String filename = headerImageFile.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        filename=CommunityUtil.generateUUID().substring(0,7)+suffix;
        //确定文件存放的全路径
        File dest=new File(uploadPath+"/"+filename);
        try {
            //存储文件
            headerImageFile.transferTo(dest);
        } catch (IOException e) {
            log.error("上传文件失败："+e.getMessage());
            throw new  RuntimeException("上传文件失败，服务器发生异常"+e.getMessage());
        }
        //更新用户头像路径（web访问路径）
        //例如http://localhost:8080/user/header/xxx.png
        User user=hostHolder.getUser();
        String headerUrl=domain+"/user/header/"+filename;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/";
    }


    //获取头像
    @RequestMapping(path = "/user/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        //服务器存放文件路径
        filename=uploadPath+"/"+filename;
        //文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try( OutputStream os=response.getOutputStream();
             FileInputStream fis=new FileInputStream(filename))
        {
            byte[] buffer=new byte[1024];
            int b=0;
            while ((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }

        } catch (IOException e) {
            log.error("读取头像失败："+e.getMessage());
        }
    }


    //更改密码
    @RequestMapping(path = "/user/updatePassword",method = RequestMethod.POST)
    public String updatePassword(Model model,String oldPassword,String newPassword,String confirmPassword){
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg","原密码不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg","新密码不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(confirmPassword)){
            model.addAttribute("confirmPasswordMsg","请先确认密码！");
            return "/site/setting";
        }
        if(newPassword.equals(oldPassword)){
            model.addAttribute("newPasswordMsg","新密码和原密码不能一致");
            return "/site/setting";
        }
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmPasswordMsg","两次密码不一致");
            return "/site/setting";
        }
        //获取当前用户
        User currentUser = hostHolder.getUser();
        //查询用户
        User userDB = userService.findUserById(currentUser.getId());
        //如果密码不正确
        if(!CommunityUtil.MD5(oldPassword+userDB.getSalt()).equals(userDB.getPassword())){
            model.addAttribute("oldPasswordMsg","密码不正确！");
            return "/site/setting";
        }else {
            newPassword=CommunityUtil.MD5(newPassword+userDB.getSalt());
            userService.updatePassword(userDB.getId(),newPassword);
            //密码更改成功重新登录
            userService.logoutByUserId(userDB.getId());
            return "redirect:/login";
        }

    }

    //个人主页
    @RequestMapping(path = "/user/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("用户不存在！");
        }
        model.addAttribute("user",user);
        //用户的点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //用户的关注数
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //用户的粉丝数
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注该页面的用户
        boolean hasFollowed=false;
        if(hostHolder.getUser()!=null){
            hasFollowed=followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }


}
