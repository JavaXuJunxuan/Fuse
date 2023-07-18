package com.xjx;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.TreeMap;

/**
 * @Author: Xjx
 * @Create: 2023/7/15 - 22:29
 */
@Slf4j
public class FixedWindowTest {
    @Test
    public void test1() throws InterruptedException {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        FixedWindowStrategy simpleWindow = new FixedWindowStrategy(redisTemplate);
        TreeMap<String, String> map = new TreeMap<>();
        map.put("SimpleWindow", "low");
        FuseModel fuseModel = new FuseModel("test", map, 1, 10, 2);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(50);
            if (simpleWindow.doHandle(fuseModel)) {
                System.out.println(System.currentTimeMillis() + "被限流了呜呜呜");
            } else {
                System.out.println(System.currentTimeMillis() + "哈哈哈哈哈");
            }
        }
    }
    @Test
    public void test2() throws InterruptedException {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        FixedWindowStrategy simpleWindow = new FixedWindowStrategy(redisTemplate);
        TreeMap<String, String> map = new TreeMap<>();
        map.put("SimpleWindow", "low");
        FuseModel fuseModel = new FuseModel("test", map, 1, 10, 2);
        //模拟时间窗口初期大量请求进来，导致整个窗口期被限流，即使空闲期间也无法处理请求
        for (int i = 0; i < 8; i++) {
            simpleWindow.doHandle(fuseModel);
        }
        for (int i = 0; i < 100; i++) {
            Thread.sleep(50);
            if (simpleWindow.doHandle(fuseModel)) {
                System.out.println(System.currentTimeMillis() + "被限流了呜呜呜");
            } else {
                System.out.println(System.currentTimeMillis() + "哈哈哈哈哈");
            }
        }
    }
    @Test
    public void test3() throws InterruptedException {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        FixedWindowStrategy simpleWindow = new FixedWindowStrategy(redisTemplate);
        TreeMap<String, String> map = new TreeMap<>();
        map.put("SimpleWindow", "low");
        FuseModel fuseModel = new FuseModel("test", map, 1, 10, 2);
        //模拟两个时间窗口突变的临界时，可以接收瞬时并发大于我们阈值流量的请求
        Thread.sleep(500);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(50);
            if (simpleWindow.doHandle(fuseModel)) {
                System.out.println(System.currentTimeMillis() + "被限流了呜呜呜");
            } else {
                System.out.println(System.currentTimeMillis() + "哈哈哈哈哈");
            }
        }
    }
}
