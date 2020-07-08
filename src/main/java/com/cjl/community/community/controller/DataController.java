package com.cjl.community.community.controller;

import com.cjl.community.community.entity.Page;
import com.cjl.community.community.service.DataService;
import com.cjl.community.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjl
 * @date 2020/4/28 15:38
 */
@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    //数据页面
    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    //统计网站uv的请求
    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    @ResponseBody
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end){
        long uv=dataService.calculateUV(start,end);
        /*model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);*/
        Map<String,Object> map=new HashMap<>();
        map.put("uvResult",uv);
        map.put("uvStartDate",start);
        map.put("uvEndDate",end);
        return CommunityUtil.getJSONString(0,"请求成功",map);
    }

    //统计活跃用户
    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    @ResponseBody
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model){
        long dau=dataService.calculateDAU(start,end);
        /*model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);*/
        Map<String,Object> map=new HashMap<>();
        map.put("dauResult",dau);
        map.put("dauStartDate",start);
        map.put("dauEndDate",end);
        return CommunityUtil.getJSONString(0,"请求成功",map);
    }
}
