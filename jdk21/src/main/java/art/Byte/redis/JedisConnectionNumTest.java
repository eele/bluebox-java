package art.Byte.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JedisConnectionNumTest {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(8);
        jedisPoolConfig.setMaxWaitMillis(10000);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "", 6379, 10000, "");

        for (int j = 0; j < 999999999; j++) {
            if (j == 60 || j == 280) {
                System.gc();
            }
            Thread.sleep(100);
            int finalJ = j;
            CyclicBarrier cyclicBarrier = new CyclicBarrier(20, () -> System.out.println("Start " + finalJ));
            for (int i = 0; i < 20; i++) {
                executorService.submit(() -> {
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }

                    Jedis jedis = null;
                    try {
                        jedis = jedisPool.getResource();
                        jedis.get("test");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                });
            }
        }
    }
}
