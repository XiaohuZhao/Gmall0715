package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.bean.SpuImage;
import com.atguigu.gmall0715.bean.SpuSaleAttr;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuManageContrller {

    @Reference
    private ManageService manageService;

    @RequestMapping("/spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId) {
        return manageService.getSpuImageList(spuId);
    }
    
    @RequestMapping("/spuSaleAttrList")
    @ResponseBody        
    public List<SpuSaleAttr> spuSaleAttrList (String spuId) {
        return manageService.getSpuSaleAttrList(spuId);
    }

    @RequestMapping("/saveSku")
    @ResponseBody
    public String saveSku (SkuInfo skuInfo) {
        manageService.saveSku(skuInfo);
        return "seccess";
    }

}
