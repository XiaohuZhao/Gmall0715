package com.atguigu.gmall0715.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    //创建连接池对象
    private JedisPool jedisPool = null;

    //JedisPool初始化
    public void initJedisPool(String host, int port) {
        //配置初始化参数
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //连接池中连接总数
        jedisPoolConfig.setMaxTotal(100);
        // 获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(10 * 1000);
        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);
        // 如果到最大数，设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 在获取连接时，检查是否有效
        jedisPoolConfig.setTestOnBorrow(true);
        // 创建连接池
        jedisPool = new JedisPool(jedisPoolConfig, host, port, 20 * 1000);
    }

    //获取jedis对象
    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    public void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
}
