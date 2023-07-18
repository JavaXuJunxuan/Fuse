package com.xjx;

import com.xjx.enums.FuseStrategyEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 抽象熔断策略类
 * @Author: Xjx
 * @Create: 2023/5/21 - 16:13
 */
public abstract class AbstractFuseStrategy {
    protected RedisTemplate<String, String> redisTemplate;
    private static final String KEY_SEPARATOR = "&&";
    private static final String BLACK_LIST = "BLACK";
    protected static final String KEY_PREFIX = "XJX_FUSE";

    public AbstractFuseStrategy(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取熔断策略名
     */
    protected abstract FuseStrategyEnum getStrategyEnum();

    protected Boolean handle(FuseModel fuseModel) {
        String blackKey = buildBlackKey(fuseModel);
        //执行黑名单
        if (redisTemplate.hasKey(blackKey)) {
            return Boolean.TRUE;
        }
        //执行熔断算法
        Boolean fuseResult = doHandle(fuseModel);
        //依据熔断算法处理结果判断是否触发熔断
        if (!fuseResult) {
            return Boolean.FALSE;
        }
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(blackKey, String.valueOf(System.currentTimeMillis()), fuseModel.getFuseTimeSecond(), TimeUnit.SECONDS);
        return Boolean.TRUE;
    }

    /**
     * 熔断算法执行（抽象类的实现类）
     * @param fuseModel
     * @return
     */
    protected abstract Boolean doHandle(FuseModel fuseModel);

    protected Boolean isFuse(FuseModel fuseModel) {
        String blackKey = buildBlackKey(fuseModel);
        //执行黑名单
        if (redisTemplate.hasKey(blackKey)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    protected static String buildBlackKey(FuseModel fuseModel) {
        return KEY_PREFIX + BLACK_LIST + KEY_SEPARATOR + fuseKey(fuseModel);
    }

    protected static String fuseKey(FuseModel fuseModel) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(fuseModel.getServiceScene()).append(KEY_SEPARATOR);
        for (Map.Entry<String, String> entry : fuseModel.getFuseFactors().entrySet()) {
            keyBuilder.append(entry.getValue()).append(KEY_SEPARATOR);
        }
        return keyBuilder.toString();
    }
}
