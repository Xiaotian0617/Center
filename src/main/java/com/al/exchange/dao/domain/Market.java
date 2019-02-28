package com.al.exchange.dao.domain;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.config.SourceConstant;
import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Market implements OnlyKey {
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
    private BigDecimal open;//24小时开盘价w
    private BigDecimal close;//24小时收盘价
    private BigDecimal volume; //24成交量
    private BigDecimal amount;//24小时成交额
    private BigDecimal ask;//卖一
    private BigDecimal bid;//买一
    private BigDecimal change;//24小时涨跌幅
    private Long timestamp;
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC
    private boolean used;//是否已经被定时取走
    private MarketSourceType type;


    public Market() {

    }

    public Market(String exchange, String symbol, String unit, BigDecimal last, BigDecimal volume, long timestamp) {
        this.exchange = ExchangeConstant.validAndGetExchangeName(exchange);
        this.symbol = symbol;
        this.unit = unit;
        this.last = last;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public Market(MarketDTO marketDTO) {
        this.id = marketDTO.getId();
        this.exchange = marketDTO.getExchange();
        this.symbol = marketDTO.getSymbol();
        this.unit = marketDTO.getUnit();
        this.tradePair = marketDTO.getTradePair();
        this.side = marketDTO.getSide();
        this.last = marketDTO.getLast();
        this.high = marketDTO.getHigh();
        this.low = marketDTO.getLow();
        this.open = marketDTO.getOpen();
        this.close = marketDTO.getClose();
        this.volume = marketDTO.getVolume();
        this.amount = marketDTO.getAmount();
        this.ask = marketDTO.getAsk();
        this.bid = marketDTO.getBid();
        this.change = marketDTO.getChange();
        this.timestamp = marketDTO.getTimestamp();
        this.onlyKey = marketDTO.onlyKey();
        this.type = marketDTO.getType();
        this.setFrom(marketDTO.getFrom());
    }

    @Override
    public String onlyKey() {
        if (onlyKey == null) {
            this.onlyKey = String.format("%s_%s_%s", exchange, symbol, unit);
        }
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
