package com.xjx;

import com.alibaba.fastjson.JSONObject;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.xjx.enums.FuseStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * 熔断查询处理器
 * @Author: Xjx
 * @Create: 2023/5/21 - 16:51
 */
@Slf4j
@Component
public class FuseHandler {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        Config config = ConfigService.getAppConfig();
        config.addChangeListener(new BlackListChangeListener(redisTemplate));
    }

    public Boolean count(FuseModel fuseModel) {
        try {
            boolean fuseSwitch = Boolean.getBoolean(environment.getProperty("fuse_switch", "false"));
            if (fuseSwitch) {
                return Boolean.FALSE;
            }
            fuseModel.verify();
            AbstractFuseStrategy strategy = FuseStrategyFactory.getStrategy(FuseStrategyEnum.MOVE_DATE_WINDOW, redisTemplate);
            Boolean result = strategy.handle(fuseModel);
            if (result) {
                log.warn("[fuse component] trigger fuse factors={}", JSONObject.toJSONString(fuseModel.getFuseFactors()));
            }
            return result;
        } catch (Exception e) {
            log.warn("[fuse component] count error stack = {}", e.getStackTrace());
            return Boolean.FALSE;
        }
    }

    public Boolean isFuse(FuseModel fuseModel) {
        try {
            boolean fuseSwitch = Boolean.getBoolean(environment.getProperty("fuse_switch", "false"));
            if (fuseSwitch) {
                return Boolean.FALSE;
            }
            Assert.notNull(fuseModel.getServiceScene(), "参数校验失败");
            Assert.notEmpty(fuseModel.getFuseFactors(), "参数校验失败");
            AbstractFuseStrategy strategy = FuseStrategyFactory.getStrategy(FuseStrategyEnum.MOVE_DATE_WINDOW, redisTemplate);
            return strategy.isFuse(fuseModel);
        } catch (Exception e) {
            log.warn("[fuse component] isFuse error stack = {}", e.getStackTrace());
            return Boolean.FALSE;
        }
    }
}
