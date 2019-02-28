package com.al.exchange.dao.domain;

import com.al.exchange.service.DataProvideService;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/23 16:49
 */
@Data
public class VolumeChange extends DataProvideService.KlineOthers {

    String id;
    @Column(name = "open")
    @JSONField(name = "o")
    BigDecimal open;
    @Column(name = "close")
    @JSONField(name = "c")
    BigDecimal close;
    @Column(name = "high")
    @JSONField(name = "h")
    BigDecimal high;
    @Column(name = "low")
    @JSONField(name = "l")
    BigDecimal low;
    @Column(name = "volume")
    @JSONField(name = "vol")
    BigDecimal volume;
    @Column(name = "time")
    @JSONField(name = "time")
    Long time;
    String type;//1m/3min/5min
    @Column(name = "onlyKey")
    String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC
    //TODO 临时改用timestamp字段
    Long timestamp;

    String measurement;

    public VolumeChange(String measurement) {
        super(measurement);
    }
}
