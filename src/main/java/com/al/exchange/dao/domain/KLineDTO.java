package com.al.exchange.dao.domain;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.annotation.Measurement;

import java.math.BigDecimal;

/**
 * K线数据
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 10/01/2018 18:52
 */
@Data
@Measurement(name = "kline")
public class KLineDTO implements OnlyKey {

    public static final String KLINE = "kline";
    public static final String KLINE_3M = "kline_3m";
    public static final String KLINE_5M = "kline_5m";
    public static final String KLINE_15M = "kline_15m";
    public static final String KLINE_30M = "kline_30m";
    public static final String KLINE_1H = "kline_1h";
    public static final String KLINE_2H = "kline_2h";
    public static final String KLINE_4H = "kline_4h";
    public static final String KLINE_6H = "kline_6h";
    public static final String KLINE_12H = "kline_12h";
    public static final String KLINE_1D = "kline_1d";
    public static final String KLINE_3D = "kline_3d";
    public static final String KLINE_1W = "kline_1w";

    private String measurement = KLINE;
    private String exchange; //交易所
    private String symbol; //币种
    private String unit; //单位

    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal volume;
    private Long timestamp;
    private String type;
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    @Override
    public String onlyKey() {
        return onlyKey;
    }

    @Override
    public String symbol() {
        return this.symbol;
    }

    @Override
    public String exchange() {
        return this.exchange;
    }

    @Override
    public String unit() {
        return this.unit;
    }

    public void setOnlyKey(String onlyKey) {
        this.onlyKey = onlyKey;
        String[] split = onlyKey.split("_");
        setExchange(split[0]);
        setSymbol(split[1]);
        setUnit(split[2]);
    }

    public void setSymbol(String symbol) {
        this.symbol = StringUtils.upperCase(symbol);
    }

    public void setUnit(String unit) {
        this.unit = StringUtils.upperCase(unit);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
