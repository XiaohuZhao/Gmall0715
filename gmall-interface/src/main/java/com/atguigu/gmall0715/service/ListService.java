package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.SkuLsInfo;
import com.atguigu.gmall0715.bean.SkuLsParams;
import com.atguigu.gmall0715.bean.SkuLsResult;

public interface ListService {
    void saveSkuInfo(SkuLsInfo skuLsInfo);
    SkuLsResult search(SkuLsParams skuLsParams);
}
