package com.al.exchange.dao.domain;

import com.al.exchange.config.ExchangeConstant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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
public class TradeDTO implements OnlyKey {

    private String measurement = "trade";

    private String tradeId;//交易所成交 id
    private String exchange;//交易所
    private String symbol;//股票代码
    private String unit;//兑换货币单位
    private String tradePaire;//e.g. symbol_unit
    private BigDecimal price;//成交价
    private BigDecimal volume;//成交量
    private BigDecimal amount;//成交额
    private String side;//方向,买卖
    private Long timestamp;//成交时间毫秒
    private String onlyKey;

    public TradeDTO() {
    }

    public TradeDTO(String exchange, String symbol, String unit) {
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
