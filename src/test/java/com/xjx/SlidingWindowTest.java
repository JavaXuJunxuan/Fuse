package com.xjx;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.TreeMap;

/**
 * @Author: Xjx
 * @Create: 2023/7/18 - 16:21
 */
@Slf4j
public class SlidingWindowTest {
    @Test
    public void test1() throws InterruptedException {
        SlidingWindowStrategy slidingWindowStrategy = new SlidingWindowStrategy();
        TreeMap<String, String> map = new TreeMap<>();
        map.put("SlidingWindow", "low");
        FuseModel fuseModel = new FuseModel("test", map, 1, 3, 1);
        for (int i = 0; i < 20; i++) {
            if (slidingWindowStrategy.doHandle(fuseModel)) {
                System.out.println("被限流了呜呜呜");
            } else {
                System.out.println("哈哈哈哈哈哈");
            }
            Thread.sleep(250);
        }


    }

}
