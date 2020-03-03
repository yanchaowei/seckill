package com.ycw.seckill.service;

import com.alibaba.fastjson.JSON;
import com.ycw.seckill.redis.KeyPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author ycw
 */
@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;

    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String valueStr = beanToString(value);
            if (valueStr == null || valueStr.length() == 0) {
                return false;
            }
            // 构建真实 key
            String realKey = prefix.getPrefix() + ":" + key;
            int expireSeconds = prefix.expireSeconds();
            if (expireSeconds <= 0) {
                jedis.set(realKey, valueStr);
            } else {
                jedis.setex(realKey, expireSeconds, valueStr);
            }
            return true;

        } finally {
            returnToPool(jedis);
        }
    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public  <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String) value;
        } else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        } else {
            return JSON.toJSONString(value);
        }
    }



    public <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }

        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 构建真实 key
            String realKey = prefix.getPrefix() + ":" +  key;
            String resultStr = jedis.get(realKey);
            T bean = stringToBean(resultStr, clazz);
            return bean;
        } finally {
            returnToPool(jedis);
        }
    }

    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 构建真实 key
            String realKey = prefix.getPrefix() + ":" + key;
            Long ret = jedis.del(realKey);
            return ret > 0;
        } finally {
            returnToPool(jedis);
        }
    }

    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 构建真实key
            String realKey = prefix.getPrefix() + ":" + key;
            Long stock = jedis.decr(realKey);
            return stock;
        } finally {
            returnToPool(jedis);
        }
    }

    public Boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 构建真实key
            String realKey = prefix.getPrefix() + ":" + key;
            Boolean exists = jedis.exists(realKey);
            return exists;
        } finally {
            returnToPool(jedis);
        }
    }

    public void incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 构建真实key
            String realKey = prefix.getPrefix() + ":" + key;
            jedis.incr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }
}
