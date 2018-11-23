package com.atguigu.gmall0715.order.contrlloer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("/trade")
    @ResponseBody
    public List<UserAddress> trade(String userId) {
        return userInfoService.findUserAddressByUserId(userId);
    }
}
