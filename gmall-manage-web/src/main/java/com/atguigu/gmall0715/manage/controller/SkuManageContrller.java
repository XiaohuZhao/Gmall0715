package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.bean.SkuLsInfo;
import com.atguigu.gmall0715.bean.SpuImage;
import com.atguigu.gmall0715.bean.SpuSaleAttr;
import com.atguigu.gmall0715.service.ListService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuManageContrller {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;

    @RequestMapping("/spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId) {
        return manageService.getSpuImageList(spuId);
    }

    @RequestMapping("/spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        return manageService.getSpuSaleAttrList(spuId);
    }

    @RequestMapping("/saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo) {
        manageService.saveSku(skuInfo);
        return "seccess";
    }

    @RequestMapping("/onSale/{skuId}")
    @ResponseBody
    public String onSale(@PathVariable String skuId) {
        //根据skuId查询skuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //skuLsInfo所有数据来源于skuInfo，必须先查询到skuInfo
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        //给skuLsInfo赋值
        skuLsInfo.copySkuInfo(skuInfo);
        listService.saveSkuInfo(skuLsInfo);
        return "success";
    }

}
