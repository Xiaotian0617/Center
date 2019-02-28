package com.al.exchange.dao.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "market")
@Data
@EqualsAndHashCode(of = "onlyKey")
public class MarketKey implements OnlyKey {
    @Column(name = "time")
    String time;
    @Column(name = "onlyKey")
    String onlyKey;
    boolean isNew;

    public MarketKey() {
    }

    public MarketKey(String time, String onlyKey) {
        this.time = time;
        this.onlyKey = onlyKey;
    }

    @Override
    public String onlyKey() {
        return this.onlyKey;
    }

    @Override
    public String symbol() {
        return StringUtils.split(this.onlyKey, "_")[1];
    }

    @Override
    public String exchange() {
        return StringUtils.split(this.onlyKey, "_")[0];
    }

    @Override
    public String unit() {
        return StringUtils.split(this.onlyKey, "_")[2];
    }
}