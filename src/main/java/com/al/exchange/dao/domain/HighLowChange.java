package com.al.exchange.dao.domain;

import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.math.BigDecimal;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/23 16:27
 */
@Data
@Measurement(name = "marketDayPriceHL")
public class HighLowChange {
    /**
     * 24小时最高
     */
    @Column(name = "high")
    private BigDecimal high;

    /**
     * 24小时最低
     */
    @Column(name = "low")
    private BigDecimal low;

    @Column(name = "onlykey")
    private String onlykey;


    public HighLowChange(String onlykey, BigDecimal high, BigDecimal low) {
        this.high = high;
        this.low = low;
        this.onlykey = onlykey;
    }
}
