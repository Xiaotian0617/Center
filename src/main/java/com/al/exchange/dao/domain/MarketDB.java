package com.al.exchange.dao.domain;

import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * 市场数据
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 10/01/2018 16:25
 */
@Data
@Measurement(name = "market")
public class MarketDB {

    @Column(name = "id")
    private Long id;
    @Column(name = "exchange", tag = true)
    private String exchange; //交易所
    @Column(name = "name")
    private String name;//symbol的全称
    @Column(name = "symbol", tag = true)
    private String symbol;//e.g. btc,eth...
    @Column(name = "unit", tag = true)
    private String unit;//兑换货币单位 BTC/USD 中的 USD
    @Column(name = "tradePair")
    private String tradePair;//e.g. BTC/USD   symbol/unit
    @Column(name = "side")
    private Integer side;//1买2卖
    @Column(name = "last")
    private Double last;//最新价
    @Column(name = "high")
    private Double high;//最高价
    @Column(name = "low")
    private Double low;//最低价
    @Column(name = "open")
    private Double open;//24小时开盘价
    @Column(name = "close")
    private Double close;//24小时收盘价
    @Column(name = "volume")
    private Double volume; //24成交量
    @Column(name = "amount")
    private Double amount;//24小时成交额
    @Column(name = "ask")
    private Double ask;//卖一
    @Column(name = "bid")
    private Double bid;//买一
    @Column(name = "change")
    private Double change;//24小时涨跌幅
    @Column(name = "time")
    private Long timestamp;
    @Column(name = "onlyKey")
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    public MarketDB() {

    }

    public MarketDB(String exchange, String symbol, String unit, Double last, Double volume, long timestamp) {
        this.exchange = exchange;
        this.symbol = symbol;
        this.unit = unit;
        this.last = last;
        this.volume = volume;
        this.timestamp = timestamp;
    }
}
