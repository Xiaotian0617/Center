package com.al.exchange.dao.domain;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.math.BigDecimal;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018年8月16日15:16:51
 */
@Data
public class Rate {

    private BigDecimal price;

    private String timestamp;

    /**
     * USDT,CNY
     */
    private String key;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
