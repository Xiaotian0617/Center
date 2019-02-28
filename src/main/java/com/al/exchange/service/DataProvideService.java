package com.al.exchange.service;

import com.al.exchange.dao.domain.*;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * file:topcoin
 * <p>
 * 数据提供接口
 *
 * @author 19:59  王楷
 * @version 19:59 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface DataProvideService {

    /**
     * 获得内存中拥有的最新的Key值对
     *
     * @return
     */
    Collection<MarketKey> getAllOnlyKeys();

    /**
     * 获得库中拥有的最新的Key值对
     *
     * @return
     */
    List<MarketKey> getAllOnlyKeysFromDB();

    /**
     * 获得库中拥有的交易所列表
     * 将从交易所的市场信息表中查出有哪些交易所的记录，汇总出系统现在可提供的交易所记录
     *
     * @return
     */
    Collection<Exchange> getAllEXchange();

    /**
     * 根据传入的币种名称，查询库中或网络上币种1兑换币种2的汇率
     * 如无法找到直接兑换比率，会进行折中兑换，支持币币兑换
     * 例如：currency1 = BTC
     * currency2 = CNY
     * 假设找不到BTC对应CNY的汇率，会先找BTC兑换USD，然后再用USD兑换CNY
     *
     * @param currency1 　币种１
     * @param currency2 　币种２
     * @return
     */
    Map<String, BigDecimal> getRateOfExchange(String currency1, String currency2);

    /**
     * 查询目前的市场快照（将从时序数据库中查出目前的市场快照）
     * 时序数据库数据来源：https://api.coinmarketcap.com/v1/ticker/
     * 截至：2018-1-13日，大概有1430多条记录，这个地方可能会做分页
     *
     * @return
     */
    List<MarketCap> getAllMarketCaps();

    /**
     * 查询目前库中某一个币种的最新市场快照
     *
     * @param id 指的是币种的全称  比如BTC  就是指  bitcoin  全小写
     * @return
     */
    MarketCap getMarketCapById(String id);

    /**
     * 查询目前的市场快照（将从时序数据库中查出目前的市场快照）
     * 时序数据库数据来源：https://api.coinmarketcap.com/v1/ticker/
     * 截至：2018-1-13日，大概有1430多条记录，这个地方可能会做分页
     * 此方法包含其市值变化记录，即某币种在24小时内的价格变化记录
     *
     * @param limit 表示价格变化记录的条数 按照rank排名从上倒下限制
     * @return
     */
    List<MarketCapVo> getAllMarketCapVo(int limit);

    /**
     * 根据币种查询所有交易所下的关于这个币种及市场信息
     *
     * @param unit 比如BTC、ETH等
     * @return
     */
    List<MarketVO> getAllMarketBySymbol(PagingParam pagingParam, String unit);

    List<MarketVO> getAllMarketBySymbol(PagingParam pagingParam, String unit, String columnName, String sortDirection);

    /**
     * 根据交易所查询这个交易所下所有币种及市场信息
     *
     * @param exchange Okex、Binance等
     * @return
     */
    List<MarketVO> getAllMarketByExchange(PagingParam pagingParam, String exchange);

    List<MarketVO> getAllMarketByExchange(PagingParam pagingParam, String exchangeName, String columnName, String sortDirection);

    /**
     * 根据币种全称获取这个币种全称的市场信息
     * @param pagingParam
     * @param allName
     * @return
     */
    List<MarketVO> getAllMarketByAllName(PagingParam pagingParam, String allName);

    /**
     * 根据交易所或币种查询这个交易所下所有币种及市场信息
     *
     * @param pagingParam
     * @param name        Okex、Binance或BTC,ETH
     * @return
     */
    List<MarketVO> getAllMarketByExchangeOrSymbol(PagingParam pagingParam, String name);

    /**
     * 根据交易所和币种查询这个交易所下这个币种的全部信息
     *
     * @param exchange 交易所  如 Okex、Binance等
     * @param symbol   币种简称 如 BTC,ETH
     * @return
     */
    List<MarketVO> getAllMarketByExchangeAndSymbol(String exchange, String symbol);

    /**
     * 根据币种全称获取这个币种全称的市场信息
     * @param pagingParam
     * @param allName
     * @return
     */
    List<MarketVO> getAllMarketByExchangeAndAllName(PagingParam pagingParam, String exchangeName, String allName);

    /**
     * 获取用户选择的K线信息 但根据后台设置
     *
     * @return
     */
    List getSettedKlineByOnlyKey(String onlyKey, String exchange, String coinName, String unitName, String timeType, Integer limitNum);


    /**
     * 获取用户选择的K线信息  但根据后台设置 有最后时间
     *
     * @param onlyKey
     * @param exchange
     * @param coinName
     * @param unitName
     * @param timeType
     * @param timestamp
     * @return
     */
    List getSettedKlineByOnlyKey(String onlyKey, String exchange, String coinName, String unitName, String timeType, Long timestamp, Integer limitNum);

    /**
     * 获取用户选择的K线信息
     *
     * @return
     */
    List getKlineByOnlyKey(String onlyKey, String exchange, String coinName, String unitName, String timeType, Integer limitNum);


    /**
     * 获取用户选择的K线信息  有最后时间
     *
     * @param onlyKey
     * @param exchange
     * @param coinName
     * @param unitName
     * @param timeType
     * @param timestamp
     * @return
     */
    List getKlineByOnlyKey(String onlyKey, String exchange, String coinName, String unitName, String timeType, Long timestamp, Integer limitNum);

    /**
     * 根据传值修改K线数据
     *
     * @return
     */
    boolean editKlineByOnlyKey(JSONObject jsonObject);

    /**
     * 用户自选根据传入的OnlyKey的集合返回订阅的信息
     *
     * @param keys
     * @return
     */
    List<MarketVO> getLastMarketByOnlyKey(List<String> keys);

    /**
     * 获取最新的全部市场信息
     *
     * @return
     */
    List<MarketDB> getAllLastMarket();

    /**
     * 从文件中获取市值信息
     */
    List<MarketCap> getMarketCapByFile();

    /**
     * 从文件中获取最全OnlyKey
     *
     * @return
     */
    List<MarketKey> getAllOnlyKeysFromFile();

    /**
     * 数据中心从Map中拿到市值信息
     *
     * @param id
     * @return
     */
    MarketCap getMarketCapFromMapById(String id);

    /**
     * 获取最新市场最高和最低价格接口
     *
     * @param jsonObject
     * @return
     */
    List<MarketMaxMinVO> getMaxAndMinPriceByOnlyKey(JSONObject jsonObject);

    List<MarketVO> getMarketsForTimeByOnlyKey(JSONObject jsonObject);

    List<MarketVO> getMarketsForTimeByExchange(JSONObject jsonObject);

    List<TradeVO> getTradesForTimeByOnlyKey(JSONObject jsonObject);

    List<TradeVO> getTradesForTimeByExchange(JSONObject jsonObject);

    List<KlineOthers> getInitMarkets();

    List<PriceVo> getRateKlineByTime(Long startTime, Long endTime, Integer num, String symbol, String unit, String type);

    /**
     * 根据币种获取相对应的7日价格
     *
     * @param coinName
     * @return
     */
    List<RiseVo> getResiFor7dByCoinName(String coinName);

    /**
     * 根据onlyKey获取相对应的7日价格
     *
     * @param onlyKey
     * @return
     */
    List<RiseVo> getResiFor7dByOnlyKey(String onlyKey);

    List<MarketVO> getAllMarketByExchangeOrSymbol(PagingParam pagingParam, String name, String columnName, String sortDirection);

//    /**
//     * 从MarketCapsMap中获取到数据并分页
//     *
//     * @param pageNum  当前页码
//     * @param pageSize 页面长度
//     * @return
//     */
//    List<MarketCap> getMarketCaps(int pageNum, int pageSize);
//
//    /**
//     * 从MarketCapsMap中获取到数据
//     *
//     * @return
//     */
//    List<MarketCap> getMarketCaps();
//
//    void refresh(List<MarketCap> list);

    @Data
    class MarketCapVo extends MarketCap {
        List<pricePoint> list;
    }

    @Measurement(name = "market_cap")
    @Data
    class pricePoint {
        @Column(name = "time")
        String time;
        @Column(name = "priceUsd")
        Double priceUsd;
    }


    @Measurement(name = "market_cap")
    @Data
    class MarketCapKeys extends MarketDB {

    }

    @Data
            //@Measurement(name = "kline_3m")
    class KlineOthers extends BaseKline {
        String id;
        @Column(name = "open")
        @JSONField(name = "o")
        BigDecimal open;
        @Column(name = "close")
        @JSONField(name = "c")
        BigDecimal close;
        @Column(name = "high")
        @JSONField(name = "h")
        BigDecimal high;
        @Column(name = "low")
        @JSONField(name = "l")
        BigDecimal low;
        @Column(name = "volume")
        @JSONField(name = "vol")
        BigDecimal volume;
        @Column(name = "time")
        @JSONField(name = "time")
        Long time;
        String type;//1m/3min/5min
        @Column(name = "onlyKey")
        String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC
        //TODO 临时改用timestamp字段
        Long timestamp;

        public Long getTimestamp() {
            return this.time;
        }

        public KlineOthers(String measurement) {
            super(measurement);
        }
    }
}