package com.al.exchange.dao.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;

@Data
public class MarketCapExt extends MarketCap {

    String id;
    @Column(name = "open")
    @JSONField(name = "o")
    BigDecimal open;
    @Column(name = "close")
    @JSONField(name = "c")
    BigDecimal close;
    @Column(name = "high")
    @JSONField(name = "h")
    BigDecimal high;
    @Column(name = "low")
    @JSONField(name = "l")
    BigDecimal low;
    @Column(name = "volume")
    @JSONField(name = "vol")
    BigDecimal volume;
    @Column(name = "time")
    @JSONField(name = "time")
    Long lastUpdated;
    String type;//1m/3min/5min
    @Column(name = "onlyKey")
    String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    //币种名称
    @Column(name = "name")
    String name;
    //币种简称
    @Column(name = "symbol", tag = true)
    @JSONField(name = "sym")
    String symbol;
    //币种市值排名
    @Column(name = "rank")
    int rank;
    //币种美元市值
    @Column(name = "priceUsd")
    @JSONField(name = "la")
    BigDecimal priceUsd;
    @Column(name = "last")
    @JSONField(name = "last")
    BigDecimal last;
    //币种比特币市值
    @Column(name = "priceBtc")
    @JSONField(name = "laBtc")
    BigDecimal priceBtc;
    //币种24小时交易量
    @Column(name = "allDayVolumeUsd")
    @JSONField(name = "vol")
    BigDecimal allDayVolumeUsd;
    //市场总值
    @Column(name = "marketCapUsd")
    @JSONField(name = "cap")
    BigDecimal marketCapUsd;
    //可购买量
    @Column(name = "availableSupply")
    @JSONField(name = "aSupply")
    BigDecimal availableSupply;
    //市场总量
    @Column(name = "totalSupply")
    @JSONField(name = "tSupply")
    BigDecimal totalSupply;
    //币种总量
    @Column(name = "maxSupply")
    BigDecimal maxSupply;
    //一小时价格变化
    @JSONField(name = "ch1h")
    @Column(name = "percentChange1h")
    BigDecimal percentChange1h;
    //二十四小时价格变化
    @JSONField(name = "ch")
    @Column(name = "percentChange24h")
    BigDecimal percentChange24h;
    //七天的价格变化
    @JSONField(name = "ch1w")
    @Column(name = "percentChange7d")
    BigDecimal percentChange7d;
    @JSONField(name = "z8")
    @Column(name = "changeForZeroHour")
    private BigDecimal changeForZeroHour;//changeForZeroHour;//changeForZeroHour 中国式涨跌幅  从北京时间00辰开始计算
    @Column(name = "changeForEightHour")
    @JSONField(name = "ch8h")
    private BigDecimal changeForEightHour;//北京时间八点涨跌幅
    @Column(name = "changeForOneMouth")
    @JSONField(name = "ch1M")
    private BigDecimal changeForOneMouth;//月涨跌幅



    private String measurement;

    public MarketCapExt() {
    }

    public MarketCapExt(String measurement) {
        this.measurement = measurement;
    }
}
