package com.atguigu.gmall0715.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.service.ManageService;
import com.atguigu.gmall0715.util.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    // 定义购物车名称
    private String COOKIE_CART_NAME = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE = 7 * 24 * 3600;

    @Reference
    private ManageService manageService;

    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {
        // 获取Cookie中所有数据
        String cartJson = CookieUtil.getCookieValue(request, COOKIE_CART_NAME, true);
        //cartJson是购物车商品的集合
        //声明一个空的集合存放cookie购物车中所有数据
        List<CartInfo> cartInfoList = new ArrayList<>();
        //购物车集合中是否包含待添加商品的标记，false表示不包含
        boolean ifExist = false;
        if (cartJson != null) {
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)) {
                    //在购物车集合中找到该商品，增加商品的数量
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    //标记该商品在购物车中存在
                    ifExist = true;
                    break;
                }
            }
        }
        if (!ifExist) {
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();
            cartInfo.createCartInfo(skuInfo, userId, skuNum);
            //将购物车中没有的商品加入购物车
            cartInfoList.add(cartInfo);
        }
        //将集合转换成json串
        String newCartJson = JSON.toJSONString(cartInfoList);
        //将json串放入cookie中
        CookieUtil.setCookie(request, response, COOKIE_CART_NAME, newCartJson, COOKIE_CART_MAXAGE, true);
    }

    /**
     * 获取cookie中购物车数据
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {
        //获取购物车数据
        String cartJson = CookieUtil.getCookieValue(request, COOKIE_CART_NAME, true);
        //转换成集合并返回
        return JSON.parseArray(cartJson, CartInfo.class);
    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, COOKIE_CART_NAME);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //取出cookie中购物车信息
        List<CartInfo> cartList = getCartList(request);
        if (cartList != null) {
            //循环比较skuId，更新选中状态
            for (CartInfo cartInfo : cartList) {
                if (cartInfo.getSkuId().equals(skuId)) {
                    cartInfo.setIsChecked(isChecked);
                }
            }
            //序列化并保存到cookie
            String cartListJson = JSON.toJSONString(cartList);
            CookieUtil.setCookie(request, response, COOKIE_CART_NAME, cartListJson, COOKIE_CART_MAXAGE, true);
        }
    }
}
