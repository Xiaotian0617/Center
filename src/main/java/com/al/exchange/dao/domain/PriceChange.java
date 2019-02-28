package com.al.exchange.dao.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceChange {
    /**
     * 24小时价格
     */
    private BigDecimal priceFor24Hour;

    /**
     * 0点价格
     */
    private BigDecimal priceFor0Hour;

    /**
     * 七天前价格
     */
    private BigDecimal priceFor7Day;

    /**
     * 一个月前价格
     */
    private BigDecimal priceFor1Mouth;

    /**
     * 一小时前价格
     */
    private BigDecimal priceFor1Hour;

    /**
     * 8点价格
     */
    private BigDecimal priceFor8Hour;

    public PriceChange() {
    }

    public PriceChange(BigDecimal priceFor24Hour, BigDecimal priceFor0Hour) {
        if (priceFor24Hour != null) {
            this.priceFor24Hour = priceFor24Hour;
        }
        if (priceFor0Hour != null) {
            this.priceFor0Hour = priceFor0Hour;
        }
    }

    public PriceChange(BigDecimal priceFor24Hour, BigDecimal priceFor0Hour, BigDecimal priceFor7Day, BigDecimal priceFor1Mouth, BigDecimal priceFor1Hour, BigDecimal priceFor8Hour) {
        if (priceFor24Hour != null) {
            this.priceFor24Hour = priceFor24Hour;
        }
        if (priceFor0Hour != null) {
            this.priceFor0Hour = priceFor0Hour;
        }
        if (priceFor7Day != null) {
            this.priceFor7Day = priceFor7Day;
        }
        if (priceFor1Mouth != null) {
            this.priceFor1Mouth = priceFor1Mouth;
        }
        if (priceFor1Hour != null) {
            this.priceFor1Hour = priceFor1Hour;
        }
        if (priceFor8Hour != null) {
            this.priceFor8Hour = priceFor8Hour;
        }
    }
}