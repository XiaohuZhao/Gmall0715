package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.PaymentInfo;

public interface PaymentService {
    /**
     * 保存订单信息
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 获取支付信息
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 更新支付信息
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    /**
     * 向支付模块发送支付结果
     */
    void sendPaymentResult(PaymentInfo paymentInfo, String result);
}
