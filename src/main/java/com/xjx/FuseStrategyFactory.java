package com.xjx;

import com.xjx.enums.FuseStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * 熔断策略工厂
 * 用于获取一个熔断策略实现类
 * @Author: Xjx
 * @Create: 2023/5/21 - 16:35
 */
@Slf4j
public class FuseStrategyFactory {
    //维护一个全局的熔断策略集合，保证单例，整个系统每个策略都只有一个实现类
    public static Map<FuseStrategyEnum, AbstractFuseStrategy> strategyContainer = new HashMap<>();
    public static AbstractFuseStrategy getStrategy(FuseStrategyEnum fuseStrategyEnum, RedisTemplate<String, String> redisTemplate) {
        try {
            AbstractFuseStrategy abstractFuseStrategy = strategyContainer.get(fuseStrategyEnum);
            if (null == abstractFuseStrategy) {
                //新建单例
                Class<?> strategyClass = Class.forName("com.xjx." + fuseStrategyEnum.getClassPrefix() + "Strategy");
                Constructor<?> constructor = strategyClass.getConstructor(redisTemplate.getClass());
                AbstractFuseStrategy strategy = (AbstractFuseStrategy) constructor.newInstance(redisTemplate);
                strategyContainer.put(fuseStrategyEnum, strategy);
                return strategy;
            }
            return abstractFuseStrategy;
        } catch (Exception e) {
            log.error("fuse strategy select fail");
            throw new RuntimeException("获取策略失败");
        }
    }
}
