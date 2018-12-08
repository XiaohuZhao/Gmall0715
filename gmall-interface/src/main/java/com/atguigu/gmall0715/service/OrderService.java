package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.OrderInfo;

public interface OrderService {
    /**
     * 保存订单信息
     */
    String  saveOrder(OrderInfo orderInfo);

    /**
     * 生成用于验证的流水号
     */
    String getTradeCode(String userId);

    /**
     * 验证流水号
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除流水号
     */
    void  delTradeCode(String userId);
}
