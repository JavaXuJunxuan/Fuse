package com.xjx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.TreeMap;

/**
 * @Author: Xjx
 * @Create: 2023/5/21 - 16:27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FuseModel {
    /**
     * 业务场景
     */
    private String serviceScene;
    /**
     * 熔断因子
     */
    private TreeMap<String, String> fuseFactors;
    /**
     * 采样时间
     */
    private Integer samplingTimeSecond;
    /**
     * 熔断阈值
     */
    private Integer threshold;
    /**
     * 熔断时间
     */
    private Integer fuseTimeSecond;
    public void verify() {
        Assert.notNull(serviceScene, "参数校验失败");
        Assert.notNull(samplingTimeSecond, "参数校验失败");
        Assert.notNull(threshold, "参数校验失败");
        Assert.notNull(fuseTimeSecond, "参数校验失败");
        Assert.notEmpty(fuseFactors, "参数校验失败");
    }
}
