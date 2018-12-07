package com.atguigu.gmall0715.cart.mapper;

import com.atguigu.gmall0715.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
    /**
     * 根据用户id获取数据库中的购物车集合
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
