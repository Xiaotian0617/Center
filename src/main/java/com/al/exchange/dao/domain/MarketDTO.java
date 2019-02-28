package com.al.exchange.dao.domain;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.config.SourceConstant;
import com.al.exchange.service.management.OnlyKeyManagement;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * Market 数据传输对象</p>
 * 内部应用间传输  从 蜘蛛 到 数据中心
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 31/01/2018 15:27
 */
@Data
public class MarketDTO implements OnlyKey {

    private String measurement = "market";

    private Long id;
    private String exchange; //交易所
    /**
     * 数据获取方式
     */
    private SourceConstant from;
    private String name;//symbol的全称
    private String symbol;//e.g. btc,eth...
    private String unit;//兑换货币单位 BTC/USD 中的 USD

    private String tradePair;//e.g. BTC/USD   symbol/unit
    private Integer side;//1买2卖
    private BigDecimal last;//最新价
    private BigDecimal high;//最高价
    private BigDecimal low;//最低价
    private BigDecimal open;//24小时开盘价
    private BigDecimal close;//24小时收盘价
    private BigDecimal volume; //24成交量
    private BigDecimal amount;//24小时成交额
    private BigDecimal ask;//卖一
    private BigDecimal bid;//买一
    private BigDecimal change;//24小时涨跌幅
    private BigDecimal average;//24小时均价
    private Long timestamp;
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC
    private MarketSourceType type;

    public MarketDTO() {

    }

    public void setFrom(String from) {
        this.from = SourceConstant.valueExist(from);
    }

//    public void setExchange(String exchange) {
//        this.exchange = ExchangeConstant.validAndGetExchangeName(exchange);
//    }

    public void setSymbol(String symbol) {
        this.symbol = StringUtils.upperCase(symbol);
    }

    public void setUnit(String unit) {
        this.unit = StringUtils.upperCase(unit);
    }

    public void setOnlyKey(String onlyKey) {
        String[] split = onlyKey.split("_");
        if (split.length != 3) {
            return;
        }
        setOnlyKey(split[0], split[1], split[2]);
    }

    public void setOnlyKey(String exchange, String symbol, String unit) {
        onlyKey = String.format("%s_%s_%s", exchange, symbol.toUpperCase(), unit.toUpperCase());
        exchange = ExchangeConstant.validAndGetExchangeName(exchange);
        setExchange(exchange);
        if (StringUtils.isEmpty(exchange)) {
            OnlyKeyManagement.addTemporaryKeys(onlyKey);
        }
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
        return this.exchange.toString();
    }

    @Override
    public String unit() {
        return this.unit;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}