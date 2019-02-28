package com.al.exchange.dao.domain;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * topcoin
 * file:topcoin
 * <p>
 *
 * @author mr.wang
 * @version 01 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Data
public class MarketVO implements OnlyKey {

    //private Long id;
    //private String name;//symbol的全称
    //private String tradePair;//e.g. BTC/USD   symbol/unit
    //private Integer side;//1买2卖
    @JSONField(name = "la")
    private BigDecimal last;//last;//最新价
    @JSONField(name = "h")
    private BigDecimal high;//high;//最高价
    @JSONField(name = "l")
    private BigDecimal low;//low;//最低价
    @JSONField(name = "o")
    private BigDecimal open;//open;//24小时开盘价
    @JSONField(name = "c")
    private BigDecimal close;//close;//24小时收盘价
    @JSONField(name = "vol")
    private BigDecimal volume;//volume; //24成交量
    @JSONField(name = "amt")
    private BigDecimal amount;//amount;//24小时成交额
    @JSONField(name = "side")
    private Integer side;//side 交易方向 1 买 2 卖
    @JSONField(name = "ask")
    private BigDecimal ask;//卖一
    @JSONField(name = "bid")
    private BigDecimal bid;//买一
    @JSONField(name = "ch")
    private BigDecimal change;//change;//24小时涨跌幅
    @JSONField(name = "z8")
    private BigDecimal changeForZeroHour;//changeForZeroHour;//changeForZeroHour 中国式涨跌幅  从北京时间00辰开始计算
    @JSONField(name = "ts")
    private long timestamp;//timestamp
    //市场的唯一标识 例如：Okex_ETH_BTC
    @JSONField(name = "key")
    private String onlyKey;//onlyKey;
    @JSONField(name = "exch")
    private String exchange;//exchange; //交易所
    @JSONField(name = "sym")
    private String symbol;//e.g. btc,eth...
    @JSONField(name = "unit")
    private String unit;//兑换货币单位 BTC/USD 中的 USD
    //是否需要发送
    @JSONField(serialize = false)
    private Boolean needSend;
    @JSONField(name = "from")
    private String from;

    private String symName;


    @JSONField(name = "ch8h")
    private BigDecimal changeForEightHour;//北京时间八点涨跌幅
    @JSONField(name = "ch1M")
    private BigDecimal changeForOneMouth;//月涨跌幅
    @JSONField(name = "ch1w")
    private BigDecimal changeForOneWeek;//周涨跌幅
    @JSONField(name = "ch1h")
    private BigDecimal changeForOneHour;//一小时涨跌幅
    //最后一次发送时间
    //private Long sendTime;


    public MarketVO() {
    }

    public MarketVO(MarketDB marketDB) {
        this.last = new BigDecimal(marketDB.getLast() == null ? 0 : marketDB.getLast()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.change = new BigDecimal(marketDB.getChange() == null ? 0 : marketDB.getChange()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.low = new BigDecimal(marketDB.getLow() == null ? 0 : marketDB.getLow()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.high = new BigDecimal(marketDB.getHigh() == null ? 0 : marketDB.getHigh()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.open = new BigDecimal(marketDB.getOpen() == null ? 0 : marketDB.getOpen()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.close = new BigDecimal(marketDB.getClose() == null ? 0 : marketDB.getClose()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.ask = new BigDecimal(marketDB.getAsk() == null ? 0 : marketDB.getAsk()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.bid = new BigDecimal(marketDB.getBid() == null ? 0 : marketDB.getBid()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.onlyKey = marketDB.getOnlyKey();
        this.timestamp = marketDB.getTimestamp();
        this.amount = new BigDecimal(marketDB.getAmount() == null ? 0 : marketDB.getAmount()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.volume = new BigDecimal(marketDB.getVolume() == null ? 0 : marketDB.getVolume()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        this.changeForZeroHour = BigDecimal.ZERO;
        this.exchange = marketDB.getExchange();
        this.symbol = marketDB.getSymbol();
        this.unit = marketDB.getUnit();
    }

    public MarketVO(Market market) {
        this.last = market.getLast();
        this.high = market.getHigh();
        this.low = market.getLow();
        this.open = market.getOpen();
        this.close = market.getClose();
        this.volume = market.getVolume();
        this.amount = market.getAmount();
        this.ask = market.getAsk();
        this.bid = market.getBid();
        this.change = market.getChange();
        this.changeForZeroHour = market.getChange();
        this.timestamp = market.getTimestamp();
        this.onlyKey = market.getOnlyKey();
        this.exchange = market.exchange();
        this.symbol = market.getSymbol();
        this.unit = market.getUnit();
        this.timestamp = market.getTimestamp();
        this.setFrom(market.getFrom().value());
    }

    public MarketVO(BigDecimal last, BigDecimal high, BigDecimal low, BigDecimal open, BigDecimal close, BigDecimal volume, BigDecimal amount, BigDecimal ask, BigDecimal bid, BigDecimal change, BigDecimal changeForZeroHour, long timestamp, String onlyKey, String exchange, String symbol, String unit, boolean needSend) {
        this.last = last;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.volume = volume;
        this.amount = amount;
        this.ask = ask;
        this.bid = bid;
        this.change = change;
        this.changeForZeroHour = changeForZeroHour;
        this.timestamp = timestamp;
        this.onlyKey = onlyKey;
        this.exchange = exchange;
        this.symbol = symbol;
        this.unit = unit;
        this.needSend = needSend;
    }

    public MarketVO(JSONObject jsonObject) {
        this.last = jsonObject.getBigDecimal("la");
        this.high = jsonObject.getBigDecimal("h");
        this.low = jsonObject.getBigDecimal("l");
        this.open = jsonObject.getBigDecimal("o");
        this.close = jsonObject.getBigDecimal("c");
        this.volume = jsonObject.getBigDecimal("v");
        this.amount = jsonObject.getBigDecimal("amt");
        this.ask = jsonObject.getBigDecimal("ask");
        this.bid = jsonObject.getBigDecimal("bid");
        this.change = jsonObject.getBigDecimal("ch");
        this.changeForZeroHour = jsonObject.getBigDecimal("ch0h");
        this.timestamp = jsonObject.getLong("ts");
        this.onlyKey = jsonObject.getString("key");
        this.exchange = jsonObject.getString("exch");
        this.symbol = jsonObject.getString("sym");
        this.unit = jsonObject.getString("unit");
        this.changeForEightHour = jsonObject.getBigDecimal("ch8h");
        this.changeForOneMouth = jsonObject.getBigDecimal("ch1M");
        this.changeForOneWeek = jsonObject.getBigDecimal("ch1w");
        this.changeForOneHour = jsonObject.getBigDecimal("ch1h");
        this.needSend = false;
    }

    public MarketVO(MarketPO marketPO) {
        this.last = marketPO.getLast();
        this.high = marketPO.getHigh();
        this.low = marketPO.getLow();
        this.open = marketPO.getOpen();
        this.close = marketPO.getClose();
        this.volume = marketPO.getVolume();
        this.amount = marketPO.getAmount();
        this.ask = marketPO.getAsk();
        this.bid = marketPO.getBid();
        this.change = marketPO.getChange();
        this.timestamp = marketPO.getTimestamp();
        this.onlyKey = marketPO.getOnlyKey();
        this.exchange = marketPO.exchange();
        this.symbol = marketPO.getSymbol();
        this.unit = marketPO.getUnit();
        this.timestamp = marketPO.getTimestamp();
        this.setFrom(marketPO.getFrom());
        this.setNeedSend(true);
        this.changeForOneHour = marketPO.getChangeForOneHour();
        this.changeForOneWeek = marketPO.getChangeForOneWeek();
        this.changeForOneMouth = marketPO.getChangeForOneMouth();
        this.changeForEightHour = marketPO.getChangeForEightHour();
        this.changeForZeroHour = marketPO.getChangeForZeroHour();
        this.symName = marketPO.getSymName();
    }

    @Override
    public String onlyKey() {
        return onlyKey;
    }

    @Override
    public String symbol() {
        return null;
    }

    @Override
    public String exchange() {
        return null;
    }

    @Override
    public String unit() {
        return null;
    }


}
