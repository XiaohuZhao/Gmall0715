package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.*;

import java.util.List;

public interface ManageService {
    /**
     * 查询所有一级分类
     */
    List<BaseCatalog1> getCatalog1();

    /**
     * 查询所有一级分类
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 查询所有一级分类
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 查询所有一级分类
     */
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 保存属性信息和属性值
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据BaseAttrValue的attrId查找BaseAttrInfo (BaseAttrValue.attrId=BaseAttrInfo.id)
     */
    BaseAttrInfo getAttrValueList(String attrId);

    /**
     * 根据条件查找SpuInfoList
     */
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    /**
     * 获取所有的销售属性
     */
    List<BaseSaleAttr> getbaseSaleAttrList();

    /**
     * 保存销售属性信息
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据SpuId查询SpuImage
     */
    List<SpuImage> getSpuImageList(String spuId);

    /**
     * 根据spuId查找所有SpuSaleAttr
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 保存skuInfo
     */
    void saveSku(SkuInfo skuInfo);

    /**
     * 根据skuId查找所有的SkuInfo
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据skuId和spuId查询SpuSaleAttr
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId, long spuId);

    /**
     * 根据spuId查询所有SkuSaleAttrValue
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     * 通过平台属性值Id查询平台属性值集合
     */
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
