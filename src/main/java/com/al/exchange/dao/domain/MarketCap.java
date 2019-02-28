package com.al.exchange.dao.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

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
@Measurement(name = "marketCap")
public class MarketCap implements OnlyKey {

    //币种全称
    @Column(name = "id", tag = true)
    String id;
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
    @Column(name = "time")
    @JSONField(name = "ts")
    Long lastUpdated;
    @Column(name = "onlyKey")
    @JSONField(name = "key")
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    @JSONField(name = "h")
    private BigDecimal high;

    @JSONField(name = "l")
    private BigDecimal low;

    public MarketCap() {
    }

    public MarketCap(MarketCapDTO marketCap) {
        this.id = marketCap.getId();
        this.allDayVolumeUsd = marketCap.getAllDayVolumeUsd();
        this.lastUpdated = marketCap.getLastUpdated();
        this.marketCapUsd = marketCap.getMarketCapUsd();
        this.maxSupply = marketCap.getMaxSupply();
        this.name = marketCap.getName();
        this.onlyKey = marketCap.getOnlyKey();
        this.percentChange1h = marketCap.getPercentChange1h();
        this.percentChange7d = marketCap.getPercentChange7d();
        this.percentChange24h = marketCap.getPercentChange24h();
        this.priceBtc = marketCap.getPriceBtc();
        this.priceUsd = marketCap.getPriceUsd();
        this.rank = marketCap.getRank();
        this.symbol = marketCap.getSymbol();
        this.totalSupply = marketCap.getTotalSupply();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MarketCap marketCap = (MarketCap) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(rank, marketCap.rank)
                .append(id, marketCap.id)
                .append(name, marketCap.name)
                .append(symbol, marketCap.symbol)
                .append(priceUsd, marketCap.priceUsd)
                .append(priceBtc, marketCap.priceBtc)
                .append(allDayVolumeUsd, marketCap.allDayVolumeUsd)
                .append(marketCapUsd, marketCap.marketCapUsd)
                .append(availableSupply, marketCap.availableSupply)
                .append(totalSupply, marketCap.totalSupply)
                .append(maxSupply, marketCap.maxSupply)
                .append(percentChange1h, marketCap.percentChange1h)
                .append(percentChange24h, marketCap.percentChange24h)
                .append(percentChange7d, marketCap.percentChange7d)
                .append(onlyKey, marketCap.onlyKey)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(id)
                .append(name)
                .append(symbol)
                .append(rank)
                .append(priceUsd)
                .append(priceBtc)
                .append(allDayVolumeUsd)
                .append(marketCapUsd)
                .append(availableSupply)
                .append(totalSupply)
                .append(maxSupply)
                .append(percentChange1h)
                .append(percentChange24h)
                .append(percentChange7d)
                .append(onlyKey)
                .toHashCode();
    }

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
        return "MarketCap";
    }

    @Override
    public String unit() {
        return "";
    }
}
