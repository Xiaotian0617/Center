package com.al.exchange.dao.domain;

import lombok.Data;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;

@Data
public class RiseVo {

    @Column(name = "last")
    BigDecimal last;

    @Column(name = "onlyKey")
    String onlyKey;

    @Column(name = "time")
    Long time;

    public RiseVo(MarketCapExt marketCapExt) {
        this.last = marketCapExt.last;
        this.onlyKey = marketCapExt.onlyKey;
        this.time = marketCapExt.lastUpdated;
    }
}
