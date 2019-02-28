package com.al.exchange.dao.domain;

import com.al.exchange.util.PointExt;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;

/**
 * 买一卖一对象存储至influxdb
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author SUNLEILEI
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/8/23
 */
@Data
public class BuyAndSellFirstPO {

    @PointExt.Measurement
    private String measurement = "BuyAndSellFirst";

    @PointExt.Tag
    @Column(name = "onlyKey", tag = true)
    private String onlyKey;

    @PointExt.Time()
    @Column(name = "time")
    private Long time;

    /**
     * sell卖,buy买
     */
    @PointExt.Tag
    @Column(name = "side", tag = true)
    private String side;

    @Column(name = "price")
    private BigDecimal price;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}

