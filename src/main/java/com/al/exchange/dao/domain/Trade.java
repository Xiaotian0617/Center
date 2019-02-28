package com.al.exchange.dao.domain;

import lombok.Data;
import org.influxdb.annotation.Measurement;

import java.math.BigDecimal;

/**
 * 交易成交详情
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 12/01/2018 13:52
 */
@Data
@Measurement(name = "trade")
public class Trade {
    private Long id;//系统内部 id
    private Long tradeId;//交易所成交 id
    private String exchange;//交易所
    private String symbol;//股票代码
    private String unit;//兑换货币单位
    private String tradePaire;//e.g. symbol_unit
    private BigDecimal price;//成交价
    private BigDecimal volume;//成交量
    private BigDecimal amount;//成交额
    private BigDecimal side;//方向,买卖
    private Long timestamp;//成交时间毫秒
}
