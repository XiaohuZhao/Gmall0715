package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.OrderDetail;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.enums.ProcessStatus;

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

    /**
     * 验证库存是否足够
     */
    boolean checkStock(OrderDetail orderDetail);

    /**
     * 根据orderId获取订单信息
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 更新订单的状态
     */
    void updateOrderStatus(String orderId, ProcessStatus processStatus);

    /**
     * 向仓库发送支付成功通知，减库存
     */
    void sendOrderStatus(String orderId);
}
