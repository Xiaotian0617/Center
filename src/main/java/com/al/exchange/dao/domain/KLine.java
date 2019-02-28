package com.al.exchange.dao.domain;

import com.al.exchange.util.PointExt;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.influxdb.annotation.Column;
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
public class KLine implements OnlyKey {
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
    @Column(name = "open")
    @JSONField(name = "o")
    private Double open;
    @Column(name = "close")
    @JSONField(name = "c")
    private Double close;
    @Column(name = "high")
    @JSONField(name = "h")
    private Double high;
    @Column(name = "low")
    @JSONField(name = "l")
    private Double low;
    @Column(name = "volume")
    @JSONField(name = "vol")
    private Double volume;
    @PointExt.Time
    @Column(name = "time")
    @JSONField(name = "time")
    private Long time;
    private String type;//1m/3min/5min
    @Column(name = "onlyKey")
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    //TODO 临时改用timestamp字段
    private Long timestamp;

    /**
     * 币币的汇率  汇率接口采用交易对unit对USDT的市场
     */
    @Column(name = "bRate")
    private BigDecimal bRate;

    /**
     * 法币的汇率 汇率默认采用CNY对USDT的市场
     */
    @Column(name = "cRate")
    private BigDecimal cRate;

    public Long getTimestamp() {
        return this.time;
    }

    public KLine() {
    }

    @Override
    public String onlyKey() {
        return this.onlyKey;
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
}
