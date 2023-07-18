package com.xjx;

import com.alibaba.fastjson.JSONObject;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * 黑名单变更监听器（用于监听人工拉黑取黑操作）
 * @Author: Xjx
 * @Create: 2023/5/21 - 17:05
 */
@Slf4j
public class BlackListChangeListener implements ConfigChangeListener {
    private final static String FUSE_ADD_BLACK_LIST = "fuse_add_black_list";
    private final static String FUSE_REMOVE_BLACK_LIST = "fuse_move_black_list";

    private RedisTemplate<String, String> redisTemplate;

    public BlackListChangeListener(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onChange(ConfigChangeEvent configChangeEvent) {
        if (configChangeEvent.isChanged(FUSE_ADD_BLACK_LIST)) {
            addBlackList(configChangeEvent.getChange(FUSE_ADD_BLACK_LIST).getNewValue());
        }
        if (configChangeEvent.isChanged(FUSE_REMOVE_BLACK_LIST)) {
            removeBlackList(configChangeEvent.getChange(FUSE_REMOVE_BLACK_LIST).getNewValue());
        }
    }

    private void removeBlackList(String value) {
        if (null == value || value == "") {
            return;
        }
        log.info("[fuse component] remove black list value = {}", value);
        FuseModel fuseModel = JSONObject.parseObject(value, FuseModel.class);
        String key = AbstractFuseStrategy.buildBlackKey(fuseModel);
        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
        }

    }

    private void addBlackList(String value) {
        if (null == value || value == "") {
            return;
        }
        log.info("[fuse component] remove black list value = {}", value);
        FuseModel fuseModel = JSONObject.parseObject(value, FuseModel.class);
        String key = AbstractFuseStrategy.buildBlackKey(fuseModel);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, String.valueOf(System.currentTimeMillis()), fuseModel.getFuseTimeSecond() * 2, TimeUnit.SECONDS);

    }
}
