package com.al.exchange.dao.domain;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.util.PointExt;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

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
public class TradeDB implements OnlyKey {


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
    private String price;//成交价


    @Column(name = "volume")
    private String volume;//成交量


    @Column(name = "amount")
    private String amount;//成交额

    @Column(name = "side")
    private String side;//方向,买卖

    @PointExt.Time
    @Column(name = "time")
    private Long timestamp;//成交时间毫秒

    @Column(name = "onlyKey")
    private String onlyKey;


    public TradeDB() {
    }

    public TradeDB(String exchange, String symbol, String unit) {
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
}
