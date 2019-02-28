package com.al.exchange.dao.domain;

import java.math.BigDecimal;

public class AbnormalChange extends AbnormalChangeKey {

    private Double max;

    private Double min;

    private Double firstTrade;

    private Double lastTrade;

    private BigDecimal amount;

    private String exchange;

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getFirstTrade() {
        return firstTrade;
    }

    public void setFirstTrade(Double firstTrade) {
        this.firstTrade = firstTrade;
    }

    public Double getLastTrade() {
        return lastTrade;
    }

    public void setLastTrade(Double lastTrade) {
        this.lastTrade = lastTrade;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}