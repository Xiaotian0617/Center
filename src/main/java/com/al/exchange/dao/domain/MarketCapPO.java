package com.al.exchange.dao.domain;

import com.al.exchange.util.PointExt;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 17:19  王楷
 * @version 17:19 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Data
public class MarketCapPO implements OnlyKey {

    @PointExt.Measurement
    String m = "market_cap";
    //币种全称
    @PointExt.Tag
    String id;
    //币种名称
    String name;
    @PointExt.Tag
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
    //最后一次更新时间
    @PointExt.Time
    @Column(name = "time")
    @JSONField(name = "ts")
    long lastUpdated;
    @Column(name = "onlyKey")
    @JSONField(name = "key")
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    @JSONField(name = "h")
    @Column(name = "high")
    private BigDecimal high;

    @JSONField(name = "l")
    @Column(name = "low")
    private BigDecimal low;

    public MarketCapPO() {

    }


    @Override
    public String toString() {
        return "MarketCap{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", rank=" + rank +
                ", priceUsd=" + priceUsd +
                ", priceBtc=" + priceBtc +
                ", allDayVolumeUsd=" + allDayVolumeUsd +
                ", marketCapUsd=" + marketCapUsd +
                ", availableSupply=" + availableSupply +
                ", totalSupply=" + totalSupply +
                ", maxSupply=" + maxSupply +
                ", percentChange1h=" + percentChange1h +
                ", percentChange24h=" + percentChange24h +
                ", percentChange7d=" + percentChange7d +
                ", lastUpdated=" + lastUpdated +
                '}';
    }


    public MarketCapPO(String id, String name, String symbol, Integer rank, BigDecimal priceUsd, BigDecimal priceBtc, BigDecimal allDayVolumeUsd, BigDecimal marketCapUsd, BigDecimal availableSupply, BigDecimal totalSupply, BigDecimal maxSupply, BigDecimal percentChange1h, BigDecimal percentChange24h, BigDecimal percentChange7d, long lastUpdated) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.rank = rank;
        this.priceUsd = priceUsd;
        this.priceBtc = priceBtc;
        this.allDayVolumeUsd = allDayVolumeUsd;
        this.marketCapUsd = marketCapUsd;
        this.availableSupply = availableSupply;
        this.totalSupply = totalSupply;
        this.maxSupply = maxSupply;
        this.percentChange1h = percentChange1h;
        this.percentChange24h = percentChange24h;
        this.percentChange7d = percentChange7d;
        this.lastUpdated = lastUpdated;
        setOnlyKey(symbol);
    }

    @Override
    public String onlyKey() {
        return onlyKey;
    }

    @Override
    public String symbol() {
        return null;
    }

    @Override
    public String exchange() {
        return null;
    }

    @Override
    public String unit() {
        return null;
    }
}

