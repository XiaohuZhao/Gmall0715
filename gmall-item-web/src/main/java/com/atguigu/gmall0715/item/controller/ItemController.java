package com.atguigu.gmall0715.item.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.bean.SkuSaleAttrValue;
import com.atguigu.gmall0715.bean.SpuSaleAttr;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @RequestMapping("/{skuId}.html")
    public String skuInfoPage(@PathVariable String skuId, HttpServletRequest request) {
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        List<SkuSaleAttrValue> saleAttrList = skuInfo.getSkuSaleAttrValueList();
        request.setAttribute("skuInfo", skuInfo);
        List<SpuSaleAttr> spuSaleAttrList = manageService
                .selectSpuSaleAttrListCheckBySku(Long.parseLong(skuId), Long.parseLong(skuInfo.getSpuId()));
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

//把列表变换成 valueid1|valueid2|valueid3 ：skuId  的 哈希表 用于在页面中定位查询
        StringBuilder jsonKey = new StringBuilder();
        Map<String, String> valuesSkuMap = new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            if (jsonKey.length() != 0) {
                jsonKey.append("|");
            }
            jsonKey.append(skuSaleAttrValue.getSaleAttrValueId());
            if ((i + 1) == skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i + 1).getSkuId())) {
                valuesSkuMap.put(jsonKey.toString(), skuSaleAttrValue.getSkuId());
                jsonKey = new StringBuilder();
            }
        }
//把map变成json串
        String valuesSkuJson = JSON.toJSONString(valuesSkuMap);
        request.setAttribute("valuesSkuJson", valuesSkuJson);

        request.setAttribute("spuSaleAttrList", spuSaleAttrList);
        return "item";
    }
}
