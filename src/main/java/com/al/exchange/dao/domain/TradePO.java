package com.al.exchange.dao.domain;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.util.PointExt;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 入库用 Trade
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018年5月15日10:04:44
 */
@Data
public class TradePO implements OnlyKey {

    @PointExt.Measurement
    private String measurement;


    @Column(name = "tradeId")
    private String tradeId;//交易所成交 id

    @PointExt.Tag
    @Column(name = "exchange", tag = true)
    private String exchange;//交易所

    @PointExt.Tag
    @Column(name = "symbol", tag = true)
    private String symbol;//股票代码

    @PointExt.Tag
    @Column(name = "unit", tag = true)
    private String unit;//兑换货币单位

    @Column(name = "tradePair")
    private String tradePaire;//e.g. symbol_unit


    @Column(name = "price")
    private BigDecimal price;//成交价


    @Column(name = "volume")
    private BigDecimal volume;//成交量


    @Column(name = "amount")
    private BigDecimal amount;//成交额

    @Column(name = "side")
    private String side;//方向,买卖

    @PointExt.Time(TimeUnit.NANOSECONDS)
    @Column(name = "time")
    private Long timestamp;//成交时间毫秒

    @Column(name = "localtime")
    private Long localtime;//本地时间毫秒

    @Column(name = "onlyKey")
    private String onlyKey;

    /**
     * 币币的汇率  汇率接口采用交易对unit对USDT的市场
     */
    @Column(name = "bRate")
    private BigDecimal bRate;

    /**
     * 法币的汇率 汇率默认采用CNY对USDT的市场
     */
    @Column(name = "cRate")
    private BigDecimal cRate;

    public TradePO() {
    }

    public TradePO(String exchange, String symbol, String unit) {
        this.exchange = ExchangeConstant.validAndGetExchangeName(exchange);
        setSymbol(symbol);
        setUnit(unit);
        setOnlyKey(this.exchange, this.symbol, this.unit);
    }

    private void setOnlyKey(String exchange, String symbol, String unit) {
        onlyKey = String.format("%s_%s_%s", exchange, symbol.toUpperCase(), unit.toUpperCase());
    }

    public String getOnlyKey() {
        return onlyKey == null ? String.format("%s_%s_%s", exchange, symbol, unit) : onlyKey;
    }

    @Override
    public String onlyKey() {
        return onlyKey;
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

    public void setSymbol(String symbol) {
        this.symbol = StringUtils.upperCase(symbol);
    }

    public void setUnit(String unit) {
        this.unit = StringUtils.upperCase(unit);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
