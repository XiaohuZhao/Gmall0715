package com.atguigu.gmall0715.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.cart.constant.CartConst;
import com.atguigu.gmall0715.cart.mapper.CartInfoMapper;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.service.CartService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Reference
    ManageService manageService;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        //查看购物车中是否有该商品
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        //根据skuId和userId查询购物车
        CartInfo cartInfoInDB = cartInfoMapper.selectOne(cartInfo);
        if (cartInfoInDB != null) {
            //该商品存在,直接修改数量
            cartInfoInDB.setSkuNum(cartInfoInDB.getSkuNum() + skuNum);
            //更改数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoInDB);
            //放入缓存
        } else {
            //该商品不存在，添加
            //根据skuId找到skuInfo
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            //创建购物车商品对象
            CartInfo newCartInfo = new CartInfo();
            newCartInfo.createCartInfo(skuInfo, userId, skuNum);
            System.out.println(newCartInfo);
            cartInfoMapper.insertSelective(newCartInfo);
            cartInfoInDB = newCartInfo;
        }

        //提出公共对象，放入redis
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfoInDB));
        //获得用户信息在redis中的过期时间
        //拼接userKey
        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userKey);
        jedis.expire(cartKey, ttl.intValue());
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        //从redis获取数据
        Jedis jedis = redisUtil.getJedis();
        //定义购物车key
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //根据key获取购物车数据
        List<String> cartJsonList = jedis.hvals(cartKey);
        if (cartJsonList != null && cartJsonList.size() > 0) {
            List<CartInfo> cartInfoList = new ArrayList<>();
            for (String cartJson : cartJsonList) {
                //每一个cartJson都是一个购物车对象
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                //加入购物车集合
                cartInfoList.add(cartInfo);
            }
            // 根据购物车id进行排序
            cartInfoList.sort(Comparator.comparing(CartInfo::getId));
            return cartInfoList;
        } else {
            //redis没有数据，去数据库查
            List<CartInfo> cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }
    }

    @Override
    public List<CartInfo> mergeCartList(List<CartInfo> cartInfoListCK, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        //cookie、mysql合并  判断条件：具有相同的skuId，将所有的cookie中的数据全部存入数据库
        for (CartInfo cartInfoCK : cartInfoListCK) {
            //使用标记，标记数据库中的商品是否和cookie中的商品id相同
            boolean isMatck = false;
            // 相同
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())) {
                    // 商品id相同，同一种商品合并数量
                    cartInfoDB.setSkuNum(cartInfoDB.getSkuNum() + cartInfoCK.getSkuNum());
                    //更新数据库
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatck = true;
                }
            }
            // 不相同
            if(!isMatck){
                //将userId存入购物车商品中
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        // 将数据库中的数据全部放入redis中
        List<CartInfo> cartInfoList = loadCartCache(userId);
        return cartInfoList;
    }

    private List<CartInfo> loadCartCache(String userId) {
        // cartInfo 有购物车价格  商品价格(skuinfo  price)
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList == null || cartInfoList.size() == 0) {
            return null;
        }

        //将购物车集合放入redsi
        Jedis jedis = redisUtil.getJedis();
        //取得key
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //声明一个map
        Map<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            //map.put(field,value) field=skuId
            map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
        }

        jedis.hmset(cartKey, map);
        jedis.close();
        return cartInfoList;
    }
}
