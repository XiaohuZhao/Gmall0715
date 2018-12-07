package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 添加购物车
     */
    void  addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 根据用户Id查询购物车数据
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 合并cookie和数据库中的数据
     */
    List<CartInfo> mergeCartList(List<CartInfo> cartInfoListCK, String userId);
}
