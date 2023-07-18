package com.xjx;

import com.xjx.enums.FuseStrategyEnum;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Xjx
 * @Create: 2023/7/18 - 15:20
 */
public class SlidingWindowStrategy extends AbstractFuseStrategy{
    private Integer windowCount = 10;
    private WindowInfo[] windowArray;

    public SlidingWindowStrategy(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
        long currentTimeMillis = System.currentTimeMillis();
        this.windowArray = new WindowInfo[windowCount];
        for (int i = 0; i < windowArray.length; i++) {
            windowArray[i] = new WindowInfo(currentTimeMillis, new AtomicInteger(0));
        }
    }

    public SlidingWindowStrategy(RedisTemplate<String, String> redisTemplate, Integer windowCount) {
        super(redisTemplate);
        this.windowCount = windowCount;
        this.windowArray = new WindowInfo[windowCount];
    }

    @Override
    protected FuseStrategyEnum getStrategyEnum() {
        return FuseStrategyEnum.Sliding_WINDOW;
    }

    @Override
    protected Boolean doHandle(FuseModel fuseModel) {
        long currentTimeMillis = System.currentTimeMillis();
        //1. 计算当前时间窗口下标
        int currentIndex = (int)(currentTimeMillis % fuseModel.getSamplingTimeSecond() * 1000 / (fuseModel.getSamplingTimeSecond() * 1000 / this.windowCount));
        //2. 更新当前窗口计数器 & 重置过期窗口计数器
        int sum = 0;
        for (int i = 0; i < windowArray.length; i++) {
            WindowInfo windowInfo = windowArray[i];
            //如果当前遍历窗口不在采样窗口时间内则重置当前遍历窗口计数器以及重新设定当前窗口采样时间
            if ((currentTimeMillis - windowInfo.getTime()) > fuseModel.getSamplingTimeSecond() * 1000) {
                windowInfo.getNumber().set(0);
                windowInfo.setTime(currentTimeMillis);
            }
            //如果当前遍历窗口是当前时间的采样窗口则需要判断当前窗口计数是否已经到达阈值
            //到达则直接取和即可，没到达只需要进行采样统计
            //即使当前窗口已经到达阈值限流了，如果当前窗口的计数器没到达阈值也需要++，因为是滑动窗口，这个窗口可能在它之前的窗口滑动之后再次进行限流判断
            //而到达阈值则不需要判断，因为已经需要限流了，再计算也没有意义，因为结果一样
            if (currentIndex == i && windowInfo.getNumber().get() < fuseModel.getThreshold()) {
                windowInfo.getNumber().incrementAndGet();
            }
            sum = sum + windowInfo.getNumber().get();
        }
        return sum >= fuseModel.getThreshold();
    }

    @Data
    private class WindowInfo {
        //窗口开始时间
        private Long time;
        //计数器
        private AtomicInteger number;

        public WindowInfo(Long time, AtomicInteger number) {
            this.time = time;
            this.number = number;
        }
    }
}
