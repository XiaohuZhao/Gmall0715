package com.atguigu.gmall0715.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.UserInfo;
import com.atguigu.gmall0715.passport.util.JwtUtil;
import com.atguigu.gmall0715.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserInfoService userInfoService;

    @Value("${token.key}")
    String signKey;


    @RequestMapping({"/index", "/"})
    public String index(HttpServletRequest request) {
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "index";
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request) {
        String salt = request.getHeader("X-forwarded-for");
        if (userInfo != null) {
            UserInfo loginUser = userInfoService.login(userInfo);
            if (loginUser != null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("userId", loginUser.getId());
                map.put("nickName", loginUser.getNickName());
                String token = JwtUtil.encode(signKey, map, salt);
                System.out.println(token);
                return token;
            }
        }
        return "fail";
    }

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(HttpServletRequest request) {
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        if (map != null) {
            String userId = (String) map.get("userId");
            UserInfo userInfo = userInfoService.verify(userId);
            if (userInfo != null) {
                return "success";
            }
        }
        return "fail";
    }
}
