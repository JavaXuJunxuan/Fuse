package com.xjx.enums;

/**
 * 熔断策略枚举类
 * @Author: Xjx
 * @Create: 2023/5/21 - 16:09
 */
public enum FuseStrategyEnum {
    /**
     * 固定窗口策略定义
     */
    Fixed_Window("固定窗口实现","FixedWindow"),
    /**
     * 滑动窗口策略定义
     */
    Sliding_WINDOW("滑动窗口实现", "SlidingWindow"),
    /**
     * 滑动时间（日志）窗口策略定义:分布式实现
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
