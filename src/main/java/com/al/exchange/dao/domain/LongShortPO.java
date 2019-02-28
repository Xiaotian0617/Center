package com.al.exchange.dao.domain;

import com.al.exchange.util.PointExt;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.math.BigDecimal;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018 2018/10/8 15:13
 */
@Data
@Measurement(name = "longShort")
public class LongShortPO implements OnlyKey {

    @PointExt.Measurement
    private String measurement = "longShort";

    @PointExt.Tag
    @Column(name = "exchange", tag = true)
    @JSONField(name = "exch")
    private String exchange; //交易所
    @PointExt.Tag
    @Column(name = "symbol", tag = true)
    @JSONField(name = "sym")
    private String symbol; //币种
    @PointExt.Tag
    @Column(name = "unit", tag = true)
    @JSONField(name = "unit")
    private String unit; //单位


    @PointExt.Time
    @Column(name = "time")
    Long lastTime;
    @Column(name = "longAmount")
    BigDecimal longAmount;
    @Column(name = "shortAmount")
    BigDecimal shortAmount;
    @Column(name = "onlyKey")
    String onlyKey;

    public void setOnlyKey(String onlyKey) {
        this.onlyKey = onlyKey;
        String[] split = onlyKey.split("_");
        this.exchange = split[0];
        this.symbol = split[1];
        this.unit = split[2];
    }

    @Override
    public String onlyKey() {
        return onlyKey;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String exchange() {
        return exchange;
    }

    @Override
    public String unit() {
        return unit;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
