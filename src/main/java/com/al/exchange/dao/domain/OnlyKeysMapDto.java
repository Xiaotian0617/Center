package com.al.exchange.dao.domain;

import com.alibaba.fastjson.JSON;
import lombok.Data;

@Data
public class OnlyKeysMapDto implements OnlyKey {

    private String onlyKey;

    private String allName;

    private Integer mKlineConf;

    private Integer dKlineConf;

    private Integer indicatorFrom;

    private Integer priceConf;

    private Integer highLowConf;

    private Integer volConf;
    private String symbol;
    private String exchange;
    private String unit;

    private Integer timeOutVal;

    public void setOnlyKey(String onlyKey) {
        this.onlyKey = onlyKey;
        String[] split = this.onlyKey.split("_");
        this.exchange = split[0];
        this.symbol = split[1];
        this.unit = split[2];
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
