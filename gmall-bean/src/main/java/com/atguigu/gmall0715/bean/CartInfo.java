package com.atguigu.gmall0715.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

public class CartInfo implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    String id;
    @Column
    String userId;
    @Column
    String skuId;
    @Column
    BigDecimal cartPrice;
    @Column
    Integer skuNum;
    @Column
    String imgUrl;
    @Column
    String skuName;

    // 实时价格 {skuInfo.price}
    @Transient
    BigDecimal skuPrice;
    // 下订单的时候，商品是否勾选 0,如果选中了1
    @Transient
    String isChecked = "0";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public BigDecimal getCartPrice() {
        return cartPrice;
    }

    public void setCartPrice(BigDecimal cartPrice) {
        this.cartPrice = cartPrice;
    }

    public Integer getSkuNum() {
        return skuNum;
    }

    public void setSkuNum(Integer skuNum) {
        this.skuNum = skuNum;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public BigDecimal getSkuPrice() {
        return skuPrice;
    }

    public void setSkuPrice(BigDecimal skuPrice) {
        this.skuPrice = skuPrice;
    }

    public String getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(String isChecked) {
        this.isChecked = isChecked;
    }

    /**
     * 使用商品信息、用户id和商品数量创建购物车内商品对象
     */
    public void createCartInfo(SkuInfo skuInfo, String userId, Integer skuNum) {
        this.skuPrice = skuInfo.getPrice();
        this.skuId = skuInfo.getId();
        this.imgUrl = skuInfo.getSkuDefaultImg();
        this.skuName = skuInfo.getSkuName();
        this.cartPrice = skuInfo.getPrice();
        this.userId = userId;
        this.skuNum = skuNum;
    }
}
