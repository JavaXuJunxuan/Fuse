package com.xjx;

import com.xjx.enums.FuseStrategyEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 固定窗口策略
 * 基于Java5中的原子类实现
 * @Author: Xjx
 * @Create: 2023/7/15 - 21:30
 */
public class FixedWindowStrategy extends AbstractFuseStrategy{
    AtomicInteger count;

    private static Map<String, AtomicInteger> countMap = new HashMap<>();
    private static long startTime = System.currentTimeMillis();

    @Override
    protected FuseStrategyEnum getStrategyEnum() {
        return FuseStrategyEnum.Fixed_Window;
    }

    @Override
    protected Boolean doHandle(FuseModel fuseModel) {
        String fuseCountKey = KEY_PREFIX + buildBlackKey(fuseModel) + "Count";
        if (countMap.containsKey(fuseCountKey)) {
            count = countMap.get(fuseCountKey);
        } else {
            count = new AtomicInteger();
            countMap.put(fuseCountKey, count);
        }
        long windowSize = fuseModel.getSamplingTimeSecond() * 1000L;
        //每次调用熔断方法的时候动态的判断窗口是否超出窗口采样时间，所以这里窗口其实并不是一个固定的值，但是对于一个流量相对稳定的环境则基本等于窗口大小
        if ((System.currentTimeMillis() - startTime) > windowSize) {
            count.set(0);
            startTime = System.currentTimeMillis();
        }
        return count.incrementAndGet() >= fuseModel.getThreshold();
    }
}
