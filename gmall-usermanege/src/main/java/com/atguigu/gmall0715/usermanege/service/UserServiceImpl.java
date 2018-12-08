package com.atguigu.gmall0715.usermanege.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.UserInfo;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.usermanege.mapper.UserAddressMapper;
import com.atguigu.gmall0715.usermanege.mapper.UserInfoMapper;
import com.atguigu.gmall0715.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String USERKEY_PREFIX = "user:";
    public String USERINFOKEY_SUFFIX = ":info";
    public int USERKEY_TIMEOUT = 60 * 60 * 24;


    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        // 对密码进行MD5加密
        String passwd = userInfo.getPasswd();
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        //更新加密后的密码
        userInfo.setPasswd(newPasswd);
        UserInfo loginUser = userInfoMapper.selectOne(userInfo);
        if (loginUser != null) {
            Jedis jedis = redisUtil.getJedis();
            String userKey = USERKEY_PREFIX + loginUser.getId() + USERINFOKEY_SUFFIX;
            jedis.setex(userKey, USERKEY_TIMEOUT, JSON.toJSONString(loginUser));
            jedis.close();
        }
        return loginUser;
    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userKey = USERKEY_PREFIX + userId + USERINFOKEY_SUFFIX;
        String userJson = jedis.get(userKey);
        jedis.expire(userKey, USERKEY_TIMEOUT);
        UserInfo userInfo = null;
        if (userJson != null && userJson.length() > 0) {
            userInfo = JSON.parseObject(userJson, UserInfo.class);
        }
        jedis.close();
        return userInfo;
    }
}
