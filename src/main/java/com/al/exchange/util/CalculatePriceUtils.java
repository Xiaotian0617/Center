package com.al.exchange.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * topcoin
 * file:topcoin
 * <p>
 *
 * @author mr.wang
 * @version 01 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
public class CalculatePriceUtils {

    /**
     * 计算价格的波动比例
     *
     * @param oldPrice
     * @param newPrice
     * @return 返回波动比例 乘过100%了
     */
    public static BigDecimal calculatePriceChange(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null || newPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("旧的价格为空了！");
            return BigDecimal.ZERO;
        }
        return (newPrice.subtract(oldPrice)).divide(oldPrice, 8, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal(100));
    }

}
