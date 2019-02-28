package com.al.exchange.dao.domain;

import com.al.exchange.util.PointExt;
import lombok.Data;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;
import java.util.Map;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/18 18:42
 */
@Data
public class PriceVo implements OnlyKey {

    @PointExt.Measurement
    private String measurement = "price_rate";

    @Column(name = "key")
    String key;
    @Column(name = "rate")
    BigDecimal rate;

    @PointExt.Tag
    @Column(name = "symbol", tag = true)
    String symbol;
    @PointExt.Tag
    @Column(name = "unit", tag = true)
    String unit;
    @Column(name = "time")
    Long time;

    public PriceVo() {
    }

    public PriceVo(String key, BigDecimal rate) {
        this.key = key;
        this.rate = rate;
    }

    public PriceVo(Map.Entry<String, BigDecimal> stringBigDecimalEntry) {
        String mapKey = stringBigDecimalEntry.getKey();
        String[] split = mapKey.split(",");
        this.key = split[0] + "_" + split[1];
        this.rate = stringBigDecimalEntry.getValue();
        this.symbol = split[0];
        this.unit = split[1];
    }

    @Override
    public String onlyKey() {
        return key;
    }

    @Override
    public String symbol() {
        String[] split = key.split(",");
        return split[0];
    }

    @Override
    public String exchange() {
        return null;
    }

    @Override
    public String unit() {
        String[] split = key.split(",");
        return split[1];
    }
}
