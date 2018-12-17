package com.atguigu.gmall0715.order.mq;

import com.atguigu.gmall0715.bean.enums.ProcessStatus;
import com.atguigu.gmall0715.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {
    @Autowired
    private OrderService orderService;

    @JmsListener(destination = "PAYMENT_RESULT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        if ("success".equals(result)) {
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            // 支付成功，通知仓库减库存
            orderService.sendOrderStatus(orderId);
            orderService.updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
        } else {
            orderService.updateOrderStatus(orderId, ProcessStatus.PAY_FAIL);
        }
    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        orderService.updateOrderStatus(orderId, "DEDUCTED".equals(status) ? ProcessStatus.WAITING_DELEVER : ProcessStatus.STOCK_EXCEPTION);
    }
}
