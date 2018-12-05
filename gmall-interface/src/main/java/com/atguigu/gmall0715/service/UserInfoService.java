package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.UserInfo;

import java.util.List;

public interface UserInfoService {
    /**
     * 查询所有用户信息
     */
    List<UserInfo> findAll();
    //

    /**
     * 根据用户id查询用户地址
     */
    List<UserAddress> findUserAddressByUserId(String userId);

    /**
     * 用户登录
     */

    UserInfo login(UserInfo userInfo);

    /**
     * 从Redis中检查是否有此用户的登录信息
     */
    UserInfo verify(String userId);
}