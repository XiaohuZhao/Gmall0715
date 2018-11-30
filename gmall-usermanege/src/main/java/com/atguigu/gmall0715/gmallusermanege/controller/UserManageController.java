package com.atguigu.gmall0715.gmallusermanege.controller;

import com.atguigu.gmall0715.bean.UserInfo;
import com.atguigu.gmall0715.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserManageController {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/findAll")
    public List<UserInfo> findAll () {
        return userInfoService.findAll();
    }
}
