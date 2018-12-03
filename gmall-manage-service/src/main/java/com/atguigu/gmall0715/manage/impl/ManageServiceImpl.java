package com.atguigu.gmall0715.manage.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.manage.constant.ManageConst;
import com.atguigu.gmall0715.manage.mapper.*;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() == null || baseAttrInfo.getId().length() == 0) {
            if (baseAttrInfo.getId().length() == 0) {
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        } else {
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList != null && attrValueList.size() > 0) {
            for (BaseAttrValue attrValue : attrValueList) {
                if (attrValue.getId().length() == 0) {
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public BaseAttrInfo getAttrValueList(String attrId) {
        //attrId = BaseAttrInfo.id
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        //查询平台属性值的集合
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getbaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //判断是插入还是更新
        String spuId = spuInfo.getId();
        if (spuId == null || spuId.length() == 0) {
            if (spuId.length() == 0) {
                spuInfo.setId(null);
            }
            spuInfoMapper.insertSelective(spuInfo);
        } else {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }
        spuId = spuInfo.getId();
        //更新销售图片数据
        //删除旧的销售图片数据
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        spuImageMapper.delete(spuImage);
        //插入新的销售图片数据
        //获取前台的销售图片数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage image : spuImageList) {
                image.setId(null);
                image.setSpuId(spuId);
                spuImageMapper.insertSelective(image);
            }
        }
        //更新销售属性参数
        //删除旧的销售属性值
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuId);
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);
        //删除旧的销售属性
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        spuSaleAttrMapper.delete(spuSaleAttr);
        //获取销售属性参数
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr saleAttr : spuSaleAttrList) {
            saleAttr.setId(null);
            saleAttr.setSpuId(spuId);
            spuSaleAttrMapper.insertSelective(saleAttr);
            //获取销售属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
            if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                    saleAttrValue.setId(null);
                    saleAttrValue.setSpuId(spuId);
                    spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        //判断id是否为空,为空是添加,不为空是更新
        String skuId = skuInfo.getId();
        if (skuId == null || skuId.length() == 0) {
            if (skuId.length() == 0) {
                skuInfo.setId(null);
                skuInfoMapper.insertSelective(skuInfo);
            }
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }
        skuId = skuInfo.getId();

        //根据spuId删掉所有的SpuImage数据
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        skuImageMapper.delete(skuImage);
        //重新插入spuImage
        //获取前端的spuImage
        List<SkuImage> imageList = skuInfo.getSkuImageList();
        if (imageList != null && imageList.size() > 0) {
            for (SkuImage image : imageList) {
                image.setId(null);
                image.setSkuId(skuId);
                skuImageMapper.insertSelective(image);
            }
        }

        //根据spuId删除所有的skuAttrValue数据
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        skuAttrValueMapper.delete(skuAttrValue);
        //重新插入skuAttrValue
        //获取前端的skuAttrValue
        List<SkuAttrValue> attrValueList = skuInfo.getSkuAttrValueList();
        if (attrValueList != null && attrValueList.size() > 0) {
            for (SkuAttrValue attrValue : attrValueList) {
                attrValue.setId(null);
                attrValue.setSkuId(skuId);
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }

        //根据spuId删除所有的skuSaleAttrValue
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
        //重新插入skuSaleAttrValue
        //获取前端的skuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
                saleAttrValue.setId(null);
                saleAttrValue.setSkuId(skuId);
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }
        System.out.println(skuInfo);
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        try {
            SkuInfo skuInfo = null;
            Jedis jedis = redisUtil.getJedis();
            //获取存入redis的skuInfo的key
            String skuInfoKey = ManageConst.getSkuInfoKey(skuId);
            //获取redis中的skuInfo的Json串
            String skuInfoJsonFromRedis = jedis.get(skuInfoKey);
            if (skuInfoJsonFromRedis == null || "".equals(skuInfoJsonFromRedis)) {
                System.out.println("没有命中缓存！");
                //没有命中缓存，开始从数据库中获取skuInfo对象
                //准备一个锁key
                String skuLockKey = ManageConst.getSkuLockKey(skuId);
                String lockKey = jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)) {
                    System.out.println("上锁，准备从数据库获取数据");
                    skuInfo = getSkuInfoFromDB(skuId);
                    String skuInfoJsonFromDB = JSON.toJSONString(skuInfo);
                    //将数据放入redis
                    jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, skuInfoJsonFromDB);
                } else {
                    //上锁失败，等待一会儿，再查询一次
                    Thread.sleep(1000);
                    getSkuInfo(skuId);
                }
            } else {
                //从redis获取到了数据
                skuInfo = JSON.parseObject(skuInfoJsonFromRedis, skuInfo.getClass());
            }
            jedis.close();
            return skuInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //以上途径获取失败，直接从数据库读取数据
        return getSkuInfoFromDB(skuId);
    }
        /*
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = null;
        try {
            // 获取redis
            Jedis jedis = redisUtil.getJedis();
            // 定义key sku:skuId:info
            String skuInfoKey = ManageConst.getSkuInfoKey(skuId); //key= sku:skuId:info
            // 从redis中取数据
            String skuInfoJsonFromRedis = jedis.get(skuInfoKey);
            if (skuInfoJsonFromRedis == null || "".equals(skuInfoJsonFromRedis)) {
                // redis中取不到数据，准备从数据库获取数据
                // 定义一个锁的key
                String skuLockKey = ManageConst.getSkuLockKey(skuId);
                // 运行命令
                String lockKey = jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)) {
                    //成功锁定，开始从数据库获取数据
                    skuInfo = getSkuInfoFromDB(skuId);
                    // 将对象转换为字符串
                    String skuInfoJsonFromDB = JSON.toJSONString(skuInfo);
                    // 将数据放入redis
                    jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, skuInfoJsonFromDB);
                    //jedis.close();
                    //return skuInfo;
                } else {
                    // 等待 ，睡一会！
                    Thread.sleep(1000);
                    // 然后继续查询
                    getSkuInfo(skuId);
                }
            } else {
                // 取得数据，从redis中取得
                skuInfo = JSON.parseObject(skuInfoJsonFromRedis, SkuInfo.class);
                //return skuInfo;
            }
            return skuInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getSkuInfoFromDB(skuId);
    }
    */

    private SkuInfo getSkuInfoFromDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        return skuInfo;
    }


    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId, long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId, spuId);
        return spuSaleAttrList;
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }
}
