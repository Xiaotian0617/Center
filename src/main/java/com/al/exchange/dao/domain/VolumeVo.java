package com.al.exchange.dao.domain;

import com.al.exchange.util.PointExt;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018 2018/11/1 18:09
 */
@Data
@Measurement(name = "kline")
public class VolumeVo {

    @PointExt.Measurement
    private String measurement = "kline";

    @Column(name = "symbol", tag = true)
    @PointExt.Tag
    private String symbol;

    @Column(name = "exchange", tag = true)
    @PointExt.Tag
    private String exchange;

    @Column(name = "volume")
    private String volume;

}
