package com.al.exchange.dao.domain;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InformationOwnDto {

    private String name;

    private String symbol;

    private String klineFrom;

    private String priceFrom;

    private String indicatorFrom;

    private String dayVolume;

    private String nameSelf;

    private BigDecimal cap;

    private BigDecimal aSupply;

    private BigDecimal tSupply;

    private BigDecimal mSupply;

    private int rank;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
