package com.al.exchange.dao.domain;

import lombok.Data;

import java.util.Date;

@Data
public class OnlyKeysConf {

    private String onlyKey;

    private String exchange;

    private String symbol;

    private String unit;

    private String status;

    private String allName;

    private Boolean isNew;

    private String klineFrom;

    private Integer mKlineConf;

    private Integer dKlineConf;

    private Integer priceConf;

    private Integer highLowConf;

    private Integer volConf;

    private Date utime;
}
