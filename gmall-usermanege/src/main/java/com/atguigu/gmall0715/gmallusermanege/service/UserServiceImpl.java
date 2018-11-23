package com.atguigu.gmall0715.gmallusermanege.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.UserInfo;
import com.atguigu.gmall0715.gmallusermanege.mapper.UserAddressMapper;
import com.atguigu.gmall0715.gmallusermanege.mapper.UserInfoMapper;
import com.atguigu.gmall0715.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findUserAddressByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }
}
