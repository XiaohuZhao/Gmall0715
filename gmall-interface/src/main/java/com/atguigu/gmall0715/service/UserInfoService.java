package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.UserInfo;

import java.util.List;

public interface UserInfoService {
    // 查询所有用户数据
    List<UserInfo> findAll();
    //根据用户id查询用户地址
    List<UserAddress> findUserAddressByUserId (String userId);
}