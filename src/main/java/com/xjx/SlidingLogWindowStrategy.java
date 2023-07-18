package com.xjx;

import com.xjx.enums.FuseStrategyEnum;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * @Author: Xjx
 * @Create: 2023/7/18 - 19:14
 */
public class SlidingLogWindowStrategy extends AbstractFuseStrategy{
    private TreeMap<Long, Long> treeMap = new TreeMap<>();

    @Override
    protected FuseStrategyEnum getStrategyEnum() {
        return FuseStrategyEnum.Sliding_LOG_WINDOW;
    }

    @Override
    protected Boolean doHandle(FuseModel fuseModel) {
        long currentTimeMillis = System.currentTimeMillis();
        //根据采样时间清理过期老数据
        if (!treeMap.isEmpty() && (currentTimeMillis - treeMap.firstKey()) > fuseModel.getSamplingTimeSecond() * 1000) {
            Set<Long> keySet = new HashSet<>(treeMap.subMap(0L, currentTimeMillis - 1000).keySet());
            for (Long key : keySet) {
                treeMap.remove(key);
            }
        }
        //遍历所有日志记录的总次数，判断是否达到限流阈值
        int sum = 0;
        for (Long value : treeMap.subMap(currentTimeMillis - 1000, currentTimeMillis).values()) {
            sum += value;
        }
        if (sum >= fuseModel.getThreshold()) {
            return true;
        }
        //Millis时间只能定位到毫秒，所以如果请求并发量很高的情况下（达到每毫秒都可以至少两次请求）可能会出现重复的key
        if (treeMap.containsKey(currentTimeMillis)) {
            treeMap.compute(currentTimeMillis, (k, v) -> v + 1);
        } else {
            treeMap.put(currentTimeMillis, 1L);
        }
        return sum >= fuseModel.getThreshold();
    }
}
