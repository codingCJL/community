package com.cjl.community.community.controller;

import com.cjl.community.community.entity.DiscussPost;
import com.cjl.community.community.entity.Page;
import com.cjl.community.community.entity.User;
import com.cjl.community.community.service.DiscussPostService;
import com.cjl.community.community.service.LikeService;
import com.cjl.community.community.service.UserService;
import com.cjl.community.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjl
 * @date 2020/4/4 23:29
 */
@Controller
public class IndexController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/",method = RequestMethod.GET)
    public String Index(Model model, Page page){
        //方法调用之前，springmvc会指定实例化Model和Page，并将Page注入Model
        page.setRows(discussPostService.selectDiscussPostRows(0));
        page.setPath("/");

        List<DiscussPost> discussPosts = discussPostService.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPostVos=new ArrayList<>();
        if(discussPosts!=null){
            for(DiscussPost post:discussPosts){
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                //查询帖子的赞数量
                long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);
                discussPostVos.add(map);
            }
        }
        model.addAttribute("discussPostVos",discussPostVos);
        return "index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    //权限不足的提示页面，以404页面处理
    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/denied";
    }
}
