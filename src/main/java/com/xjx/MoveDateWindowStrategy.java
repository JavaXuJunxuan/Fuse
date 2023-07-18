package com.xjx;

import com.xjx.enums.FuseStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.concurrent.TimeUnit;

/**
 * 滑动时间窗口策略
 * 基于redis zset实现
 * @Author: Xjx
 * @Create: 2023/5/21 - 16:43
 */
@Slf4j
public class MoveDateWindowStrategy extends AbstractFuseStrategy{
    private static Long count;
    private static Long currTime;
    public MoveDateWindowStrategy(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected FuseStrategyEnum getStrategyEnum() {
        return FuseStrategyEnum.MOVE_DATE_WINDOW;
    }

    @Override
    protected Boolean doHandle(FuseModel fuseModel) {
        currTime = System.currentTimeMillis();
        long windowSize = fuseModel.getSamplingTimeSecond() * 1000L;
        String fuseKey = KEY_PREFIX + buildBlackKey(fuseModel);
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        //删除已过窗口数据
        zSetOperations.removeRangeByScore(fuseKey, 0, (currTime - windowSize));
        //加入新节点
        zSetOperations.add(fuseKey, currTime + random(), currTime);
        //设置key的过期时间
        redisTemplate.expire(fuseKey, fuseModel.getSamplingTimeSecond() * 2, TimeUnit.SECONDS);
        count = zSetOperations.count(fuseKey, (currTime - windowSize), currTime);
        return count.compareTo(new Long(fuseModel.getThreshold())) >= 0;
    }

    private String random() {
        String random = "" + Math.random();
        return random.substring(random.length() - 4);
    }
}
