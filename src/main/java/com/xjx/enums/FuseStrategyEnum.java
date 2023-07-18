package com.xjx.enums;

/**
 * 熔断策略枚举类
 * @Author: Xjx
 * @Create: 2023/5/21 - 16:09
 */
public enum FuseStrategyEnum {
    /**
     * 滑动时间（日志）窗口策略定义
     */
    MOVE_DATE_WINDOW("滑动窗口实现", "MoveDateWindow");

    private final String desc;
    private final String classPrefix;

    FuseStrategyEnum(String desc, String classPrefix) {
        this.desc = desc;
        this.classPrefix = classPrefix;
    }

    public String getDesc() {
        return desc;
    }

    public String getClassPrefix() {
        return classPrefix;
    }
}
