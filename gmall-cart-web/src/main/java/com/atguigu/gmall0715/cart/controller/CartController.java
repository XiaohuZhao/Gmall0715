package com.atguigu.gmall0715.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.anootation.LoginRequire;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.service.CartService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @RequestMapping({"/index", "/"})
    public String index(HttpServletRequest request) {
        return "success";
    }

    @LoginRequire(autoRedirect = false)
    @RequestMapping("/addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {
        //获取skuId
        String skuId = request.getParameter("skuId");
        //获取购买商品的数量
        String skuNum = request.getParameter("skuNum");
        // 如果用户已经登陆，获取用户id，但不必要登录，使用@LoginRequire(autoRedirect = false)注解
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            //用户已登录
            cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));
        } else {
            ////未登录
            cartCookieHandler.addToCart(request, response, skuId, userId, Integer.parseInt(skuNum));
        }

        //获取商品信息，存入request域
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        // 商品数量
        request.setAttribute("skuNum", skuNum);

        return "success";
    }

    @RequestMapping("/cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response) {
        // 取得用户id
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = null;
        if (userId != null) {
            //已登录
            //合并Cookie中的数据
            //获取cookie中的数据
            List<CartInfo> cartInfoListCK = cartCookieHandler.getCartList(request);
            if (cartInfoListCK != null && cartInfoListCK.size() > 0) {
                //cookie中的数据不为空，开始合并
                cartInfoList = cartService.mergeCartList(cartInfoListCK, userId);
                //合并之后，删除cookie中的数据
                cartCookieHandler.deleteCartCookie(request, response);
            } else {
                //cookie中没有数据，从redis中获取数据
                cartInfoList = cartService.getCartList(userId);
            }
        } else {
            //未登录，从cookie中获取数据
            cartInfoList = cartCookieHandler.getCartList(request);
        }
        request.setAttribute("cartInfoList", cartInfoList);
        return "cartList";
    }
}