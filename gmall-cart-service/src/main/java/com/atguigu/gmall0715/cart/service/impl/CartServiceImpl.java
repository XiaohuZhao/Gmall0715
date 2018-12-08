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
            List<CartInfo> cartInfoList = getCartInfoListFromJson(cartJsonList);
            // 根据购物车id进行排序
            cartInfoList.sort(Comparator.comparing(CartInfo::getId));
            return cartInfoList;
        } else {
            //redis没有数据，去数据库查
            return loadCartCache(userId);
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
            if (!isMatck) {
                //将userId存入购物车商品中
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }

        // 将数据库中的数据全部放入redis中
        //合并cookie和数据库中已勾选的购物车商品
        //获取数据库中购物车的数据
        List<CartInfo> cartInfoSelectedListDB = loadCartCache(userId);
        for (CartInfo cartInfoDB : cartInfoSelectedListDB) {
            for (CartInfo cartInfoCK : cartInfoListCK) {
                //匹配skuId
                if (cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())) {
                    //当Cookie中的数据已选中时，设置redis中的数据也选中
                    //此合并可能会丢失redis购物车物品的选中状态，因为数据库中没有储存选中状态
                    if ("1".equals(cartInfoCK.getIsChecked())) {
                        cartInfoDB.setIsChecked(cartInfoCK.getIsChecked());
                        checkCart(cartInfoDB.getSkuId(), cartInfoCK.getIsChecked(), userId);
                    }
                }
            }
        }
        return cartInfoSelectedListDB;
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        Jedis jedis = redisUtil.getJedis();
        //取得redis中的购物车信息
        //取得用户购物车key
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //取redis中的信息
        String cartJson = jedis.hget(cartKey, skuId);
        //反序列化
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        if (cartInfo != null) {
            //更新购物车商品的选中状态
            cartInfo.setIsChecked(isChecked);
            //序列化存入redis
            String cartCheckedJson = JSON.toJSONString(cartInfo);
            jedis.hset(cartKey, skuId, cartCheckedJson);
            //获取储存用户购物车中已勾选的key
            String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
            if (isChecked.equals("1")) {
                //如果是选中，将商品存入已勾选的key中
                jedis.hset(userCheckedKey, skuId, cartCheckedJson);
            } else {
                //如果是取消选中，将商品从已勾选的商品中删除
                jedis.hdel(userCheckedKey, skuId);
            }
            jedis.close();
        }
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        //获取redis中的key
        String checkedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedListJson = jedis.hvals(checkedKey);
        return getCartInfoListFromJson(cartCheckedListJson);
    }

    /**
     * 将cartInfoListJson转换成List<CartInfo>
     */
    private List<CartInfo> getCartInfoListFromJson(List<String> cartInfoListJson) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        for (String cartInfoJson : cartInfoListJson) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            cartInfoList.add(cartInfo);
        }
        return cartInfoList;
    }

    /**
     * 根据用户id获取数据库中购物车数据
     */
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
