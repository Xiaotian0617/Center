package com.al.exchange.dao.domain;

import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * 市场最大最小值展示VO
 */
@Data
@Measurement(name = "market")
public class MarketMaxMinVO {


    @Column(name = "maxLast")
    private String maxLast;

    @Column(name = "minLast")
    private String minLast;

    @Column(name = "time")
    private Long time;

}
