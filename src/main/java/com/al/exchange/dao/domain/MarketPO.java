package com.al.exchange.dao.domain;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.util.PointExt;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;

/**
 * 入库用 market
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 31/01/2018 11:54
 */
@Data
public class MarketPO implements OnlyKey {

    @PointExt.Measurement
    private String measurement = "market";

    private Long id;
    @PointExt.Tag
    @Column(name = "exchange", tag = true)
    private String exchange; //交易所
    @PointExt.Tag
    @Column(name = "name")
    private String name;//symbol的全称
    @PointExt.Tag
    @Column(name = "symbol", tag = true)
    private String symbol;//e.g. btc,eth...
    @PointExt.Tag
    @Column(name = "unit", tag = true)
    private String unit;//兑换货币单位 BTC/USD 中的 USD

    @Column(name = "tradePair")
    private String tradePair;//e.g. BTC/USD   symbol/unit
    @Column(name = "side")
    private Integer side;//1买2卖
    @Column(name = "last")
    private BigDecimal last;//最新价
    @Column(name = "high")
    private BigDecimal high;//最高价
    @Column(name = "low")
    private BigDecimal low;//最低价
    @Column(name = "open")
    private BigDecimal open;//24小时开盘价
    @Column(name = "close")
    private BigDecimal close;//24小时收盘价
    @Column(name = "volume")
    private BigDecimal volume; //24成交量
    @Column(name = "amount")
    private BigDecimal amount;//24小时成交额
    @Column(name = "ask")
    private BigDecimal ask;//卖一
    @Column(name = "bid")
    private BigDecimal bid;//买一
    @Column(name = "change")
    @JSONField(name = "ch")
    private BigDecimal change;//change;//24小时涨跌幅
    @JSONField(name = "z8")
    private BigDecimal changeForZeroHour;//changeForZeroHour;//changeForZeroHour 中国式涨跌幅  从北京时间00辰开始计算
    @JSONField(name = "ch8h")
    private BigDecimal changeForEightHour;//北京时间八点涨跌幅
    @JSONField(name = "ch1M")
    private BigDecimal changeForOneMouth;//月涨跌幅
    @JSONField(name = "ch1w")
    private BigDecimal changeForOneWeek;//周涨跌幅
    @JSONField(name = "ch1h")
    private BigDecimal changeForOneHour;//一小时涨跌幅

    private String symName;

    /**
     * 币币的汇率  汇率接口采用交易对unit对USDT的市场
     */
    private BigDecimal bRate;

    /**
     * 法币的汇率 汇率默认采用CNY对USDT的市场
     */
    private BigDecimal cRate;

    private BigDecimal average;//24小时均价
    @PointExt.Time
    @Column(name = "time")
    private Long timestamp;
    @Column(name = "onlyKey")
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    @Column(name = "type")
    private String type;

    @Column(name = "from")
    private String from;

    public MarketPO() {
    }

    public MarketPO(String exchange, String symbol, String unit, BigDecimal last, BigDecimal volume, Long timestamp) {
        this.exchange = ExchangeConstant.validAndGetExchangeName(exchange);
        setSymbol(symbol);
        setUnit(unit);
        this.last = last;
        this.volume = volume;
        this.timestamp = timestamp;
        setOnlyKey(exchange, this.symbol, this.unit);
    }

    public MarketPO(String exchange, String symbol, String unit) {
        this(exchange, symbol, unit, null, null, null);
    }

    public MarketPO(Market market) {
        this(market.getExchange(), market.getSymbol(), market.getUnit(), market.getLast(), market.getVolume(), market.getTimestamp());
    }

    public void setSymbol(String symbol) {
        this.symbol = StringUtils.upperCase(symbol);
    }

    public void setUnit(String unit) {
        this.unit = StringUtils.upperCase(unit);
    }

    public void setOnlyKey(String exchange, String symbol, String unit) {
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
}
