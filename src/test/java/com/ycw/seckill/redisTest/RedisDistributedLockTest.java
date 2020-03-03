package com.ycw.seckill.redisTest;


import redis.clients.jedis.Jedis;

// 下面就以秒杀库存数量为场景，测试下上面实现的分布式锁的效果。具体测试代码如下：
public class RedisDistributedLockTest {
    static int n = 500;
    public static void secskill() {
        System.out.println("该用户成功秒杀到一件商品剩余库存:" + --n);
    }

    public static void main(String[] args) {
        Runnable runnable = () -> {
            RedisDistributedLock lock = null;
            String unLockIdentify = null;
            try {
                Jedis conn = new Jedis("172.19.240.60",6389);
                lock = new RedisDistributedLock(conn, "test1");
                unLockIdentify = lock.acquire();
                if (unLockIdentify != null) {
                    System.out.println(unLockIdentify);
                    System.out.println(Thread.currentThread().getName() + "正在运行");
                    secskill();
                } else {
                    System.out.println(unLockIdentify);
                    System.out.println("获取锁失败，不能秒杀。");
                }
            } finally {
                if (lock != null) {
                    lock.release(unLockIdentify);
                }
            }
        };

        for (int i = 0; i < 2; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}