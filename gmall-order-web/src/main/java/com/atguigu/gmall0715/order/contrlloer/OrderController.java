package com.atguigu.gmall0715.order.contrlloer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.anootation.LoginRequire;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.OrderDetail;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.enums.OrderStatus;
import com.atguigu.gmall0715.bean.enums.ProcessStatus;
import com.atguigu.gmall0715.service.CartService;
import com.atguigu.gmall0715.service.OrderService;
import com.atguigu.gmall0715.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserInfoService userInfoService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    @LoginRequire
    @RequestMapping("/trade")
    public String trade(HttpServletRequest request) {
        //获取用户id
        String userId = (String) request.getAttribute("userId");
        //生成用于验证的流水号，防止用户重复提交同一订单
        String tradeNo = orderService.getTradeCode(userId);
        request.setAttribute("tradeNo", tradeNo);
        //获取选中的购物车商品列表
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);
        //获取收货人地址
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        //将收货地址加入request域
        request.setAttribute("userAddressList", userAddressList);
        //订单信息集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.createOrderDetail(cartInfo);
            orderDetailList.add(orderDetail);
        }
        //将orderDetailList存到request域
        request.setAttribute("orderDetailList", orderDetailList);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        return "trade";
    }

    @LoginRequire
    @RequestMapping("/submitOrder")
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request) {
        //获取userId
        String userId = (String) request.getAttribute("userId");
        // 检查tradeCode
        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag) {
            request.setAttribute("errMsg", "该页面已失效，请重新结算!");
            return "tradeFail";
        }

        //初始化参数，未支付
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        //保存orderInfo
        String orderId = orderService.saveOrder(orderInfo);
        // 删除tradeNo
        orderService.delTradeCode(userId);
        return "redirect://payment.gmall.com/index?orderId=" + orderId;
    }

}
