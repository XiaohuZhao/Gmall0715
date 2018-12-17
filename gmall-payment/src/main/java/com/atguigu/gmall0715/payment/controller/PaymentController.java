package com.atguigu.gmall0715.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall0715.anootation.LoginRequire;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.PaymentInfo;
import com.atguigu.gmall0715.bean.enums.PaymentStatus;
import com.atguigu.gmall0715.payment.config.AlipayConfig;
import com.atguigu.gmall0715.service.OrderService;
import com.atguigu.gmall0715.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

@Controller
public class PaymentController {
    @Reference
    OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;


    @RequestMapping("/index")
    @LoginRequire
    public String index(HttpServletRequest request) {
        // 获取订单的id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        request.setAttribute("orderId", orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }

    @RequestMapping(value = "/alipay/submit", method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response) {
        // 获取订单Id
        String orderId = request.getParameter("orderId");
        // 取得订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.createPaymentInfo(orderInfo);

        // 保存信息
        paymentService.savePaymentInfo(paymentInfo);

        // 支付宝参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        // 生成支付宝所需参数
        Map<String, Object> bizContentMap = paymentInfo.getAlipayParams();
        // 将map变成json
        String Json = JSON.toJSONString(bizContentMap);
        alipayRequest.setBizContent(Json);
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        return form;
    }

    @RequestMapping(value = "/alipay/callback/return", method = RequestMethod.GET)
    public String callbackReturn() {
        //设置支付成功后返回订单详情页
        return "redirect:" + AlipayConfig.return_order_url;
    }

    /**
     * 支付过程结束后，发送异步通知，确认并记录用户已付款，通知电商模块
     */
    @RequestMapping(value = "/alipay/callback/notify", method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String, String> paramMap, HttpServletRequest request) throws AlipayApiException {

        // 判断是否支付成功
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8", AlipayConfig.sign_type);
        if (!flag) {
            //支付不成功，不需要发送消息，电商查询不到支付结果，就代表没有支付成功
            return "fail";
        }
        // 判断结束
        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
            //获取交易编号
            String out_trade_no = paramMap.get("out_trade_no");
            //用于查询数据库的对象
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            PaymentInfo paymentInfoHas = paymentService.getPaymentInfo(paymentInfo);
            // 查单据是否处理
            if (paymentInfoHas.getPaymentStatus() == PaymentStatus.PAID || paymentInfoHas.getPaymentStatus() == PaymentStatus.ClOSED) {
                // 订单已被支付或者关闭，返回支付不成功
                return "fail";
            } else {
                // 修改
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                // 设置状态
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                // 设置创建时间
                paymentInfoUpd.setCallbackTime(new Date());
                // 设置内容
                paymentInfoUpd.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no, paymentInfoUpd);

                // 支付成功，告诉电商
                paymentService.sendPaymentResult(paymentInfoHas, "success");
                return "success";
            }
        }
        return "fail";
    }

    /**
     * 仅用于测试支付后的动作，没有实际作用
     */
    @RequestMapping("/sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo, @RequestParam("result") String result) {
        paymentService.sendPaymentResult(paymentInfo, result);
        return "sent payment result";
    }

}
