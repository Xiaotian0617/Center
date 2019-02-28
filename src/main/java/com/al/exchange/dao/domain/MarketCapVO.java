package com.al.exchange.dao.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
public class MarketCapVO implements OnlyKey {
    //币种名称
    String name;
    //币种简称
    @JSONField(name = "sym")
    String symbol;
    //币种市值排名
    int rank;
    //币种美元市值
    @JSONField(name = "la")
    BigDecimal priceUsd;
    //币种比特币市值
    @JSONField(name = "laBtc")
    BigDecimal priceBtc;
    //币种24小时交易量
    @JSONField(name = "vol")
    BigDecimal allDayVolumeUsd;
    //市场总值
    @JSONField(name = "cap")
    BigDecimal marketCapUsd;
    //一小时价格变化
    @JSONField(name = "ch1h")
    BigDecimal percentChange1h;
    //二十四小时价格变化
    @JSONField(name = "ch")
    BigDecimal percentChange24h;
    //七天的价格变化
    @JSONField(name = "ch1w")
    BigDecimal percentChange7d;
    @JSONField(name = "z8")
    private BigDecimal changeForZeroHour;//changeForZeroHour;//changeForZeroHour 中国式涨跌幅  从北京时间00辰开始计算
    @JSONField(name = "ch8h")
    private BigDecimal changeForEightHour;//北京时间八点涨跌幅
    @JSONField(name = "ch1M")
    private BigDecimal changeForOneMouth;//月涨跌幅
    //最后一次更新时间
    @JSONField(name = "ts")
    long lastUpdated;
    @JSONField(name = "key")
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    @JSONField(name = "h")
    private BigDecimal high;

    @JSONField(name = "l")
    private BigDecimal low;

    @Override
    public String toString() {
        return "MarketCap{" +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", rank=" + rank +
                ", priceUsd=" + priceUsd +
                ", priceBtc=" + priceBtc +
                ", allDayVolumeUsd=" + allDayVolumeUsd +
                ", marketCapUsd=" + marketCapUsd +
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

        MarketCapVO marketCap = (MarketCapVO) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(rank, marketCap.rank)
                .append(name, marketCap.name)
                .append(symbol, marketCap.symbol)
                .append(priceUsd, marketCap.priceUsd)
                .append(priceBtc, marketCap.priceBtc)
                .append(allDayVolumeUsd, marketCap.allDayVolumeUsd)
                .append(marketCapUsd, marketCap.marketCapUsd)
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
                .append(name)
                .append(symbol)
                .append(rank)
                .append(priceUsd)
                .append(priceBtc)
                .append(allDayVolumeUsd)
                .append(marketCapUsd)
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
