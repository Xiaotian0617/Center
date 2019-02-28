package com.al.exchange.service;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.dao.domain.*;
import com.al.exchange.service.management.MarketCapManagement;
import com.al.exchange.service.management.OnlyKeyManagement;
import com.al.exchange.service.management.SourceSettingManagement;
import com.al.exchange.task.MarketInfoEnhanceTask;
import com.al.exchange.task.PriceRateTask;
import com.al.exchange.task.SaveMsgToHardDriveTask;
import com.al.exchange.util.InfluxDbMapper;
import com.al.exchange.util.InfluxResultExt;
import com.al.exchange.util.OperationFileUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * file:topcoin
 * <p>
 * 文件简要说明
 *
 * @author 9:53  王楷
 * @version 9:53 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Service
@Slf4j
public class DataProvideServiceImpl implements DataProvideService {

    @Autowired
    private InfluxDbMapper influxDbMapper;

    @Autowired
    OperationFileUtils operationFileUtils;

    @Autowired
    MarketCapManagement marketCapManagement;

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    @Autowired
    ExchangeRealtimeMarketService exchangeRealtimeMarketService;

    @Autowired
    MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Autowired
    PriceRateTask priceRateTask;

    @Autowired
    SourceSettingManagement sourceSettingManagement;

    @Autowired
    InfluxResultExt influxResultExt;

    @Autowired
    ExchangeConstant exchangeConstant;

    private final Long ONEMINTIME = 60000L;
    private final Long ONEHOUR = 60000L * 60L;
    private final Long ONEDAY = 60000L * 24L * 60L;


    private final Map<String, KeyLinData> keyLinesMap = new HashMap<String, KeyLinData>() {{
        put("kline", new KeyLinData("Kline1m", "1d", "kline_1m", ONEMINTIME));
        put("kline_1m", new KeyLinData("Kline1m", "1d", "kline_1m", ONEMINTIME));
        put("kline_3m", new KeyLinData("Kline3m", "3d", "kline_1m", 3L * ONEMINTIME));
        put("kline_5m", new KeyLinData("Kline5m", "5d", "kline_1m", 5L * ONEMINTIME));
        put("kline_10m", new KeyLinData("Kline10m", "10d", "kline_1m", 10L * ONEMINTIME));
        put("kline_15m", new KeyLinData("Kline15m", "15d", "kline_1m", 15L * ONEMINTIME));
        put("kline_30m", new KeyLinData("Kline30m", "30d", "kline_1m", 30L * ONEMINTIME));
        put("kline_1h", new KeyLinData("Kline1h", "60d", "kline_1m", ONEHOUR));
        put("kline_2h", new KeyLinData("Kline2h", "120d", "kline_1m", 2L * ONEHOUR));
        put("kline_4h", new KeyLinData("Kline4h", "240d", "kline_1m", 4L * ONEHOUR));
        put("kline_6h", new KeyLinData("Kline6h", "360d", "kline_1m", 6L * ONEHOUR));
        put("kline_12h", new KeyLinData("Kline12h", "720d", "kline_1m", 12L * ONEHOUR));
        put("kline_1d", new KeyLinData("Kline1d", "1dINF", "kline_1d", ONEDAY));
        put("kline_3d", new KeyLinData("Kline3d", "3dINF", "kline_1d", 3L * ONEDAY));
        put("kline_7d", new KeyLinData("Kline7d", "7dINF", "kline_1d", 7L * ONEDAY));
        put("kline_1w", new KeyLinData("Kline1w", "1wINF", "kline_1d", 7L * ONEDAY));
        put("kline_1M", new KeyLinData("Kline1M", "1MINF", "kline_1d", 31L * ONEDAY));
    }};


    /**
     * 获得库中拥有的最新的Key值对
     *
     * @return
     */
    @Override
    public Collection<MarketKey> getAllOnlyKeys() {
        log.debug("获得库中拥有的最新的Key值对");
        List<MarketKey> marketKeys = onlyKeyManagement.onlyKeys();
        return marketKeys;
    }

    /**
     * 获得库中拥有的最新的Key值对
     */
    @Override
    public List<MarketKey> getAllOnlyKeysFromDB() {
        log.debug("获得库中拥有的最新的Key值对");
        String queryMarketSql = "SELECT onlyKey  FROM TopCoinDB.autogen.market where time>now() - 1d  GROUP BY  *  order by time desc  limit 1";
        QueryResult queryMarketResult = influxDbMapper.query(queryMarketSql);
        return new InfluxDBResultMapper().toPOJO(queryMarketResult, MarketKey.class);
    }

    /**
     * 获得库中拥有的交易所列表
     * 将从交易所的市场信息表中查出有哪些交易所的记录，汇总出系统现在可提供的交易所记录
     */
    @Override
    public Collection<Exchange> getAllEXchange() {
        log.debug("获得库中拥有的交易所列表");
        List<Exchange> collect = exchangeConstant.getExchangeConstantsMap().entrySet().stream()
                .filter(stringExchangeBookEntry -> stringExchangeBookEntry.getValue().getSort() != null)
                .filter(stringExchangeBookEntry -> stringExchangeBookEntry.getValue().getSort() != -1)
                .sorted(Comparator.comparing(o -> o.getValue().getSort()))
                .map(exchangeBook -> {
                    Exchange exchange = new Exchange();
                    exchange.setCName(exchangeBook.getValue().getCname());
                    exchange.setEName(exchangeBook.getValue().getExchange());
                    exchange.setImg(exchangeBook.getValue().getImg() + exchangeBook.getValue().getExchange().toLowerCase() + ".png");
                    return exchange;
                }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 根据传入的币种名称，查询库中或网络上币种1兑换币种2的汇率
     * 如无法找到直接兑换比率，会进行折中兑换，支持币币兑换
     * 例如：currency1 = BTC
     * currency2 = CNY
     * 假设找不到BTC对应CNY的汇率，会先找BTC兑换USD，然后再用USD兑换CNY
     *
     * @param currency1 　币种１
     * @param currency2 　币种２
     */
    @Override
    public Map<String, BigDecimal> getRateOfExchange(String currency1, String currency2) {
        log.debug("查询库中或网络上币种1兑换币种2的汇率");
        return priceRateTask.getMap();
    }

    /**
     * 查询目前的市场快照（将从时序数据库中查出目前的市场快照）
     * 时序数据库数据来源：https://api.coinmarketcap.com/v1/ticker/
     * 截至：2018-1-13日，大概有1430多条记录，这个地方可能会做分页
     */
    @Override
    public List<MarketCap> getAllMarketCaps() {
        log.debug("查询目前的市场快照（将从时序数据库中查出目前的市场快照）");
        String querySql = "SELECT * FROM \"TopCoinDB\".\"autogen\".\"marketCap\" where time > now() - 30m  group by * order by time desc limit 1";
        QueryResult queryResult = influxDbMapper.query(querySql);
        List<MarketCap> list = influxResultExt.toPOJO(queryResult, MarketCap.class,"marketCap");
        return list.stream().sorted(Comparator.comparingInt(MarketCap::getRank)).collect(Collectors.toList());
    }

    /**
     * 查询目前库中某一个币种的最新市场快照
     *
     * @param id 指的是币种的全称  比如BTC  就是指  bitcoin  全小写
     */
    @Override
    public MarketCap getMarketCapById(String id) {
        log.debug("查询目前库中某一个币种的最新市场快照");
        String querySql = "SELECT * FROM \"TopCoinDB\".\"autogen\".\"marketCap\" WHERE \"id\"='" + id + "' and time > now() - 30m order by time desc limit 1 ";
        QueryResult queryResult = influxDbMapper.query(querySql);
        List<MarketCap> list = influxResultExt.toPOJO(queryResult, MarketCap.class,"marketCap");
        return list.size() == 0 ? null : list.get(0);
    }

    /**
     * 查询目前的市场快照（将从时序数据库中查出目前的市场快照）
     * 时序数据库数据来源：https://api.coinmarketcap.com/v1/ticker/
     * 截至：2018-01-13日，大概有1430多条记录，这个地方可能会做分页
     * 此方法包含其市值变化记录，即某币种在24小时内的价格变化记录
     *
     * @param limit 表示价格变化记录的条数 按照rank排名从上倒下限制
     */
    @Override
    public List<MarketCapVo> getAllMarketCapVo(int limit) {
        log.debug("查询目前的市场快照（将从时序数据库中查出目前的市场快照）,此方法包含其市值变化记录");
        List<MarketCapVo> li = new ArrayList<>();
        getAllMarketCaps().forEach((MarketCap marketCap) -> {
            MarketCapVo marketCapVo = new MarketCapVo();
            BeanUtils.copyProperties(marketCap, marketCapVo);
            if (marketCap.getRank() <= limit) {
                String querySql = "SELECT \"time\",\"priceUsd\" FROM \"TopCoinDB\".\"autogen\".\"marketCap\" WHERE time > now() - 24h AND \"id\"='" + marketCap.getId() + "'";
                QueryResult queryResult = influxDbMapper.query(querySql);
                marketCapVo.setList(new InfluxDBResultMapper().toPOJO(queryResult, pricePoint.class));
            }
            li.add(marketCapVo);
        });
        return li;
    }

    /**
     * 根据币种查询所有交易所下的关于这个币种及市场信息
     *
     * @param symbol 比如BTC、ETH等
     */
    @Override
    public List<MarketVO> getAllMarketBySymbol(PagingParam pagingParam, String symbol) {
        return getAllMarketBySymbol(pagingParam,symbol,null,null);
    }

    /**
     * 根据交易所查询这个交易所下所有币种及市场信息
     *
     * @param exchange Okex、Binance等
     */
    @Override
    public List<MarketVO> getAllMarketByExchange(PagingParam pagingParam, String exchange) {
        return getAllMarketByExchange(pagingParam,exchange,null,null);
    }

    @Override
    public List<MarketVO> getAllMarketByExchangeOrSymbol(PagingParam pagingParam, String name, String columnName, String sortDirection) {
        log.debug("根据交易所或币种查询这个交易所下所有币种及市场信息");
        String exchangeName = name.substring(0, 1).toUpperCase() + name.substring(1);
        List<MarketVO> allMarketByExchange = getAllMarketByExchange(pagingParam, exchangeName,columnName,sortDirection);
        if (allMarketByExchange.size() > 0) {
            return allMarketByExchange;
        }
        return getAllMarketBySymbol(pagingParam, name.toUpperCase(),columnName,sortDirection);
    }

    @Override
    public List<MarketVO> getAllMarketBySymbol(PagingParam pagingParam, String symbol, String columnName, String sortDirection) {
        log.debug("根据币种查询所有交易所下的关于这个币种及市场信息");
        List<MarketVO> marketVOS = new ArrayList<>();
        List<MarketKey> marketKeys = onlyKeyManagement.getOnlyKeysBySymbol(symbol);
        marketKeys.forEach(getMarketKeyConsumer(marketVOS));
        if (StringUtils.hasText(columnName)&&StringUtils.hasText(sortDirection)){
            marketVOS = marketVOS.stream().sorted((o1, o2) -> sortedColumnName(o1,o2,columnName,sortDirection)).collect(Collectors.toList());
        }
        if (pagingParam.getLimit() != null || pagingParam.getOffset() != null) {
            int skipNum = (pagingParam.getOffset() - 1) * pagingParam.getLimit();
            int limitNum = pagingParam.getLimit();
            marketVOS = marketVOS.stream().skip(skipNum < 0 ? 0 : skipNum).limit(limitNum).collect(Collectors.toList());
        }
        return marketVOS;
    }

    @Override
    public List<MarketVO> getAllMarketByExchange(PagingParam pagingParam, String exchangeName, String columnName, String sortDirection) {
        log.debug("根据交易所查询这个交易所下所有币种及市场信息");
        List<MarketVO> marketVOS = new ArrayList<>();
        List<MarketKey> marketKeys = onlyKeyManagement.getOnlyKeysByExchange(exchangeName);
        marketKeys.forEach(getMarketKeyConsumer(marketVOS));
        if (StringUtils.hasText(columnName)&&StringUtils.hasText(sortDirection)){
            marketVOS = marketVOS.stream().sorted((o1, o2) -> sortedColumnName(o1,o2,columnName,sortDirection)).collect(Collectors.toList());
        }
        if (pagingParam.getLimit() != null || pagingParam.getOffset() != null) {
            int skipNum = (pagingParam.getOffset() - 1) * pagingParam.getLimit();
            int limitNum = pagingParam.getLimit();
            marketVOS = marketVOS.stream().skip(skipNum < 0 ? 0 : skipNum).limit(limitNum).collect(Collectors.toList());
        }
        return marketVOS;
    }

    @Override
    public List<MarketVO> getAllMarketByAllName(PagingParam pagingParam, String allName) {
        List<MarketVO> marketVOS = marketInfoEnhanceTask.getMarketMap().values().stream()
                .filter(marketVO -> marketVO.getSymName().equals(allName))
                .collect(Collectors.toList());
        if (pagingParam.getLimit() != null || pagingParam.getOffset() != null) {
            int skipNum = (pagingParam.getOffset() - 1) * pagingParam.getLimit();
            int limitNum = pagingParam.getLimit();
            marketVOS = marketVOS.stream().skip(skipNum < 0 ? 0 : skipNum).limit(limitNum).collect(Collectors.toList());
        }
        return marketVOS;
    }

    /**
     * 根据交易所或币种查询这个交易所下所有币种及市场信息
     *
     * @param pagingParam
     * @param name        Okex、Binance或BTC,ETH
     */
    @Override
    public List<MarketVO> getAllMarketByExchangeOrSymbol(PagingParam pagingParam, String name) {
        return getAllMarketByExchangeOrSymbol(pagingParam,name,null,null);
    }

    private int sortedColumnName(MarketVO marketVO, MarketVO marketVO1, String columnName, String sortDirection) {
        if (StringUtils.isEmpty(columnName) || StringUtils.isEmpty(sortDirection)) {
            //如果前台没有传入的话，按照查询顺序
            return 1;
        }
        JSONObject marketVOJson = JSON.parseObject(JSON.toJSONString(marketVO));
        JSONObject marketVO1Json = JSON.parseObject(JSON.toJSONString(marketVO1));
        if (StringUtils.isEmpty(marketVOJson.get(columnName)) || StringUtils.isEmpty(marketVO1Json.get(columnName))) {
            //如果前台传入的列名 数据中无内容
            return 1;
        }
        int sort = 1;
        BigDecimal o1 = marketVOJson.getBigDecimal(columnName);
        BigDecimal o2 = marketVO1Json.getBigDecimal(columnName);
        sort = o1.compareTo(o2);
        return "desc".equalsIgnoreCase(sortDirection) ? -sort : sort;
    }

    @Override
    public List<MarketVO> getAllMarketByExchangeAndSymbol(String exchange, String symbol) {
        List<MarketVO> markets = marketInfoEnhanceTask.getMarketMap().entrySet().stream().filter(map -> {
            String[] split = map.getKey().split("_");
            return exchange.equals(split[0]) && symbol.equals(split[1]);
        }).map(map -> map.getValue()).collect(Collectors.toList());
        return markets;
    }

    @Override
    public List<MarketVO> getAllMarketByExchangeAndAllName(PagingParam pagingParam, String exchangeName, String allName) {
        List<MarketVO> marketVOS = marketInfoEnhanceTask.getMarketMap().values().stream()
                .filter(marketVO -> marketVO.getSymName().equals(allName))
                .filter(marketVO -> marketVO.getExchange().equals(exchangeName))
                .collect(Collectors.toList());
        if (pagingParam.getLimit() != null || pagingParam.getOffset() != null) {
            int skipNum = (pagingParam.getOffset() - 1) * pagingParam.getLimit();
            int limitNum = pagingParam.getLimit();
            marketVOS = marketVOS.stream().skip(skipNum < 0 ? 0 : skipNum).limit(limitNum).collect(Collectors.toList());
        }
        return marketVOS;
    }

    @Override
    public List getSettedKlineByOnlyKey(String onlyKey, String exchange, String coinName, String unitName, String timeType, Integer limitNum) {
        return getSettedKlineByOnlyKey(onlyKey, exchange, coinName, unitName, timeType, null, limitNum);
    }

    @Override
    public List getSettedKlineByOnlyKey(String onlyKey, String exchange, String coinName, String unitName, String timeType, Long timestamp, Integer limitNum) {
        log.debug("获取用户选择的K线信息 有最后时间点");
        String querySql = "SELECT * FROM \"TopCoinDB\".\"autogen\".\"%s\" WHERE %s  " +
                " \"exchange\"='%s' AND \"symbol\"='%s' " +
                "AND \"unit\"='%s' order by time desc limit %s";
        String kline = "kline";
        String time = "";
        if (StringUtils.hasText(timeType) && !Objects.equals("kline_1m", timeType)) {
            kline = timeType;
        }
        return getKlineForSetting(onlyKey, exchange, coinName, unitName, timeType, timestamp, limitNum, querySql, kline);
    }

    private Consumer<MarketKey> getMarketKeyConsumer(List<MarketVO> marketVOS) {
        return marketKey -> {
            if (marketInfoEnhanceTask.getMarketMap().get(marketKey.onlyKey()) == null) {
                return;
            }
            marketVOS.add(marketInfoEnhanceTask.getMarketMap().get(marketKey.onlyKey()));
        };
    }


    /**
     * 获取用户选择的K线信息
     */
    @Override
    public List getKlineByOnlyKey(String onlyKey, String exchange, String coinName, String unitName, String timeType, Integer limitNum) {
        log.debug("获取用户选择的K线信息");
        return getKlineByOnlyKey(onlyKey, exchange, coinName, unitName, timeType, null, limitNum);
    }

    /**
     * 获取用户选择的K线信息 有最后时间点
     */
    @Override
    public List getKlineByOnlyKey(String onlyKey, String exchange, String coinName, String unitName, String timeType, Long timestamp, Integer limitNum) {
        log.debug("获取用户选择的K线信息 有最后时间点");
        String querySql = "SELECT * FROM \"TopCoinDB\".\"autogen\".\"%s\" WHERE %s  " +
                " \"exchange\"='%s' AND \"symbol\"='%s' " +
                "AND \"unit\"='%s' order by time desc limit %s";
        String kline = "kline";
        String time = "";
        if (StringUtils.hasText(timeType) && !Objects.equals("kline_1m", timeType)) {
            kline = timeType;
        }
        if (("kline_1d".equalsIgnoreCase(kline) || "kline_3d".equalsIgnoreCase(kline) ||
                "kline_1w".equalsIgnoreCase(kline) || "kline_1M".equalsIgnoreCase(kline))) {
            String endStr = kline.substring(kline.length() - 1);
            String substring = endStr.toUpperCase();
            kline = kline.substring(0, kline.length() - 1) + substring;
        }
        return getKlineForDB(exchange, coinName, unitName, timeType, timestamp, limitNum, querySql, kline);
    }

    private List getKlineForSetting(String onlyKey, String exchange, String coinName, String unitName, String timeType, Long timestamp, Integer limitNum, String querySql, String kline) {
        SourceSettingManagement.OnlykeySetted marketSettedMap = sourceSettingManagement.getMarketSettedMap(onlyKey);
        if (marketSettedMap == null) {
            return new ArrayList();
        }
        SourceSettingManagement.MarketDayKlineSetted marketDayKlineSetted = marketSettedMap.getDayKlineSetted();
        SourceSettingManagement.MarketMinuteKlineSetted marketMinuteKlineSetted = marketSettedMap.getMinuteKlineSetted();
        //进行K线的转换处理 根据后台设置获取不同来源的K线 由于现在日K线之上的K线获取来源固定为交易所获取，排除这四种后接下来按照分钟K线设置处理
        boolean checkDayNull = ("kline_1d".equalsIgnoreCase(kline) || "kline_3d".equalsIgnoreCase(kline) ||
                "kline_1w".equalsIgnoreCase(kline) || "kline_1M".equalsIgnoreCase(kline)) && marketDayKlineSetted != null;
        if (checkDayNull) {
            kline = getKlineNameByDay(timeType, marketDayKlineSetted, kline);
        } else if (marketMinuteKlineSetted != null) {
            kline = getKlineNameByMinute(timeType, marketMinuteKlineSetted, kline);
        } else {
            return new ArrayList();
        }
        return getKlineForDB(exchange, coinName, unitName, timeType, timestamp, limitNum, querySql, kline);
    }

    private List getKlineForDB(String exchange, String coinName, String unitName, String timeType, Long timestamp, Integer limitNum, String querySql, String kline) {
        String time;
        if (!StringUtils.hasText(kline)) {
            return new ArrayList();
        }
        if (limitNum == null || limitNum == 0) {
            limitNum = 500;
        }
        if (!Objects.equals(timestamp, null) && !Objects.equals(timestamp, 0L)) {
            if (timestamp.compareTo(0L) < 0) {
                // 负数
                time = "time <= " + (-timestamp) + "000000000  AND  ";
            } else {
                time = "time >= " + timestamp + "000000000  AND  ";
            }
        } else {
            long timeMillis = System.currentTimeMillis() - (keyLinesMap.get(timeType).minTime * limitNum);
            time = "time >=  " + timeMillis * 1000000L + " AND ";
        }

//        String format = String.format(querySql, keyLinesMap.get(timeType).getLibraryName(),
//                keyLinesMap.get(timeType).getStrategyName(), kline, time, exchange, coinName, unitName, limitNum);
        String format = String.format(querySql, kline, time, exchange, coinName, unitName, limitNum);
        log.info(format);
        QueryResult queryResult = influxDbMapper.query(format);
        List list = getKlineList(queryResult, kline);
        Collections.reverse(list);
        return list;
    }


    private String getKlineNameByDay(String timeType, SourceSettingManagement.MarketDayKlineSetted marketDayKlineSetted, String kline) {
        KeyLinData keyLinData = keyLinesMap.get(timeType);
        if (keyLinData == null) {
            return null;
        }
        //boolean checkResult = "kline_1d".equals(keyLinData.getSettingFrom());
        String[] split = timeType.split("_");
        switch (marketDayKlineSetted.getSoureSetted()) {
            case 1:
                return "calc_kline" + "_" + split[1].toUpperCase();
            case 2:
                return "kline" + "_" + split[1].toUpperCase();
            case 3:
                return "no_vol_kline" + "_" + split[1].toUpperCase();
            default:
                return null;
        }
    }

    private String getKlineNameByMinute(String timeType, SourceSettingManagement.MarketMinuteKlineSetted marketMinuteKlineSetted, String kline) {
        KeyLinData keyLinData = keyLinesMap.get(timeType);
        if (keyLinData == null) {
            return null;
        }
        boolean checkResult = "kline_1m".equals(keyLinData.getSettingFrom());
        if (marketMinuteKlineSetted.getSoureSetted() == 1) {
            if (checkResult) {
                kline = "calc_kline";
            } else {
                kline = "calc_" + timeType;
            }
        }
        if (marketMinuteKlineSetted.getSoureSetted() == 2) {
            if (checkResult) {
                kline = "kline";
            } else {
                kline = timeType;
            }
        }
        if (marketMinuteKlineSetted.getSoureSetted() == 3) {
            if (checkResult) {
                kline = "no_vol_kline";
            } else {
                kline = "no_vol" + timeType;
            }
        }
        String[] split = timeType.split("_");
        if (split.length == 2 && !"1m".equalsIgnoreCase(split[1])) {
            kline = kline + "_" + split[1];
        }
        return kline;
    }

    private String getKlineNameForSetted(String exchange, String coinName, String unitName, String timeType) {
        StringBuilder onlykey = new StringBuilder().append(exchange).append("_").append(coinName).append("_").append(unitName);
        SourceSettingManagement.OnlykeySetted marketSettedMap = sourceSettingManagement.getMarketSettedMap(onlykey.toString());
        if (marketSettedMap == null) {
            return null;
        }
        boolean checkMinTimeType = "kline_1m".equalsIgnoreCase(timeType) || "kline".equalsIgnoreCase(timeType);
        SourceSettingManagement.MarketMinuteKlineSetted minuteKlineSetted = marketSettedMap.getMinuteKlineSetted();
        boolean checkMinSetted = (minuteKlineSetted != null);
        if (checkMinTimeType && checkMinSetted) {
            //如果满足1分钟K线并且后台设置不为空
            switch (minuteKlineSetted.getSoureSetted()) {
                case 1:
                    return "kline";
                case 2:
                    return "calc_kline";
            }
        }
        boolean checkDayTimeType = "kline_1d".equalsIgnoreCase(timeType);
        SourceSettingManagement.MarketDayKlineSetted dayKlineSetted = marketSettedMap.getDayKlineSetted();
        boolean checkDaySetted = (minuteKlineSetted != null);
        if (checkDayTimeType && checkDaySetted) {
            //如果满足1天K线并且后台设置不为空
            switch (dayKlineSetted.getSoureSetted()) {
                case 1:
                    return "kline_1d";
                case 2:
                    return "calc_kline_1d";
            }
        }
        return null;
    }


    /**
     * 根据传值修改K线数据
     * 1.先检测库中是否有相应的时间点的数据
     * 2.如果有的话，进行覆盖操作
     */
    @Override
    public boolean editKlineByOnlyKey(JSONObject jsonObject) {
        KLine newKline = JSONObject.parseObject(jsonObject.toJSONString(), KLine.class);
        String[] keys = newKline.getOnlyKey().split("_");
        String kline = "kline";
        if (!Objects.equals(newKline.getType(), null) && !Objects.equals(newKline.getType(), "") && !Objects.equals("kline_1m", newKline.getType())) {
            kline = newKline.getType() + "";
        }
        String querySql = "SELECT * FROM \"TopCoinDB\".\"autogen\".\"%s\" WHERE \"exchange\"='%s' AND \"symbol\"='%s' AND \"unit\"='%s' AND \"time\" = %s ";
        QueryResult queryResult = influxDbMapper.query(String.format(querySql, kline, keys[0], keys[1], keys[2], newKline.getTimestamp() + "000000000"));
        List<KLine> li = getKlineList(queryResult, "kline");
        if (li == null || li.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 用户自选根据传入的OnlyKey的集合返回订阅的信息
     *
     * @param keys
     * @return
     */
    @Override
    public List<MarketVO> getLastMarketByOnlyKey(List<String> keys) {
        List<MarketVO> marketVOS = new ArrayList<>();
        for (MarketVO value : marketInfoEnhanceTask.getMarketMap().values()) {
            for (String key : keys) {
                if (key.equals(value.getOnlyKey())) {
                    marketVOS.add(value);
                }
            }
        }
        return marketVOS;
    }

    /**
     * 获取最新的全部市场信息
     *
     * @return
     */
    @Override
    public List<MarketDB> getAllLastMarket() {
        String querySql = "select  *  from market where time > now() - 1h group by *  order by time desc limit 1";
        return new InfluxDBResultMapper().toPOJO(influxDbMapper.query(querySql), MarketDB.class);
    }

    /**
     * 从文件中获取市值信息
     */
    @Override
    public List<MarketCap> getMarketCapByFile() {
        String jsonArray = operationFileUtils.readFile(SaveMsgToHardDriveTask.marketCapDirectoryName, SaveMsgToHardDriveTask.marketCapFileName);
        if (jsonArray != null) {
            return JSONObject.parseArray(jsonArray, MarketCap.class);
        }
        return null;
    }

    /**
     * 从文件中获取最全OnlyKey
     *
     * @return
     */
    @Override
    public List<MarketKey> getAllOnlyKeysFromFile() {
        String jsonArray = operationFileUtils.readFile(SaveMsgToHardDriveTask.onlyKeyDirectoryName, SaveMsgToHardDriveTask.onlyKeyFileName);
        if (jsonArray != null) {
            return JSONObject.parseArray(jsonArray, MarketKey.class);
        }
        return null;
    }

    /**
     * 数据中心从Map中拿到市值信息
     *
     * @param id
     * @return
     */
    @Override
    public MarketCap getMarketCapFromMapById(String id) {
        return marketCapManagement.getMarketCaps(id);
    }

    /**
     * 获取最新市场最高和最低价格接口
     *
     * @param jsonObject
     * @return
     */
    @Override
    public List<MarketMaxMinVO> getMaxAndMinPriceByOnlyKey(JSONObject jsonObject) {
        String querySql = "SELECT max(\"last\") AS \"maxLast\", min(\"last\") AS \"minLast\" FROM \"TopCoinDB\".\"autogen\".\"market\" WHERE  time < now() AND time > %s000000000 AND \"exchange\"='%s' AND \"symbol\"='%s' AND \"unit\"='%s'";
        Long time = jsonObject.getLong("time");
        String[] onlyKey = jsonObject.getString("onlyKey").split("_");
        if (onlyKey.length != 3) {
            throw new RuntimeException();
        }
        return new InfluxDBResultMapper().toPOJO(influxDbMapper.query(String.format(querySql, time, onlyKey[0], onlyKey[1], onlyKey[2])), MarketMaxMinVO.class);
    }

    @Override
    public List<MarketVO> getMarketsForTimeByOnlyKey(JSONObject jsonObject) {
        return getMarketsForTime(jsonObject);
    }

    @Override
    public List<MarketVO> getMarketsForTimeByExchange(JSONObject jsonObject) {
        String exchange = jsonObject.getString("exchange");
        String unitName = jsonObject.getString("unitName");
        String coinName = jsonObject.getString("coinName");
        Long startTime = jsonObject.getLong("startTime");
        Long endTime = jsonObject.getLong("endTime");
        String querySql = "select  *  from \"TopCoinDB\".\"autogen\".\"market\" WHERE  %s  AND \"time\" >= %s AND \"time\" < %s ";
        List<MarketDB> marketDBS = new InfluxDBResultMapper().toPOJO(influxDbMapper.queryForNanoseconds(String.format(querySql, getExchangeSelcetStr(exchange, coinName, unitName), startTime + "000000", endTime + "000000")), MarketDB.class);
        return marketDBS.stream().map(marketDB -> getMarketVOByMarketDB(marketDB)).collect(Collectors.toList());
    }

    private List<MarketVO> getMarketsForTime(JSONObject jsonObject) {
        String onlyKey = jsonObject.getString("onlyKey");
        String[] keys = onlyKey.split("_");
        String exchangeName = keys[0];
        String symbolName = keys[1];
        String unitName = keys[2];
        Long startTime = jsonObject.getLong("startTime");
        Long endTime = jsonObject.getLong("endTime");
        String querySql = "select  *  from \"TopCoinDB\".\"autogen\".\"market\" WHERE \"exchange\"='%s' AND \"symbol\"='%s' AND \"unit\"='%s' AND \"time\" >= %s AND \"time\" < %s ";
        List<MarketDB> marketDBS = new InfluxDBResultMapper().toPOJO(influxDbMapper.queryForNanoseconds(String.format(querySql, exchangeName, symbolName, unitName, startTime + "000000", endTime + "000000")), MarketDB.class);
        return marketDBS.stream().map(marketDB -> getMarketVOByMarketDB(marketDB)).collect(Collectors.toList());
    }

    private MarketVO getMarketVOByMarketDB(MarketDB marketDB) {
        MarketVO marketVO = new MarketVO(marketDB);
        MarketCap marketCapsBySymbol = marketCapManagement.getMarketCapsBySymbol(marketDB.getSymbol());
        marketVO.setSymName(marketCapsBySymbol == null ? "" : marketCapsBySymbol.getName());
        return marketVO;
    }

    private TradeVO getTradeVOByTradeDB(TradeDB tradeDB) {
        TradeVO tradeVO = new TradeVO(tradeDB);
        MarketCap marketCapsBySymbol = marketCapManagement.getMarketCapsBySymbol(tradeDB.getSymbol());
        tradeVO.setSymName(marketCapsBySymbol == null ? "" : marketCapsBySymbol.getName());
        return tradeVO;
    }

    @Override
    public List<TradeVO> getTradesForTimeByOnlyKey(JSONObject jsonObject) {
        String onlyKeyString = jsonObject.getString("onlyKey");
        Long startTime = jsonObject.getLong("startTime");
        Long endTime = jsonObject.getLong("endTime");
        String[] onlyKeys = onlyKeyString.split(",");
        List<TradeVO> trades = new ArrayList<>(1000);
        for (String onlyKey : onlyKeys) {
            trades.addAll(getTradesForTime(onlyKey, startTime, endTime));
        }
        return trades;
    }

    @Override
    public List<TradeVO> getTradesForTimeByExchange(JSONObject jsonObject) {
        String exchange = jsonObject.getString("exchange");
        String unitName = jsonObject.getString("unitName");
        String coinName = jsonObject.getString("coinName");
        Long startTime = jsonObject.getLong("startTime");
        Long endTime = jsonObject.getLong("endTime");
        String querySql = "select  *  from \"TopCoinDB\".\"autogen\".\"trade\" WHERE %s AND \"time\" >= %s AND \"time\" < %s ";
        List<TradeDB> tradeDBS = new InfluxDBResultMapper().toPOJO(influxDbMapper.queryForNanoseconds(String.format(querySql, getExchangeSelcetStr(exchange, coinName, unitName), startTime, endTime)), TradeDB.class);
        return tradeDBS.stream().map(tradeDB -> getTradeVOByTradeDB(tradeDB)).collect(Collectors.toList());
    }

    @Override
    public List<KlineOthers> getInitMarkets() {
        String sql = "SELECT * FROM \"Kline1d\".\"1dINF\".\"no_vol_kline_1d\"  WHERE time > now() - 3d group by * order by time desc limit 1";
        List<KlineOthers> klineOthers = influxResultExt.toPOJO(influxDbMapper.query(sql), KlineOthers.class, "no_vol_kline_1d");
        return klineOthers;
    }

    @Override
    public List<PriceVo> getRateKlineByTime(Long startTime, Long endTime, Integer num, String symbol, String unit, String type) {
        String measurement = "";
        String sqlMeasurement = "";
        String sql = "SELECT * FROM %s WHERE time >= %s AND time <= %s AND \"symbol\"='%s' AND \"unit\"='%s' ";
        switch (type) {
            case "kline":
                sqlMeasurement = "\"PriceRate\".\"1d\".\"priceRate1m\"";
                measurement = "priceRate1m";
                break;
            case "kline_3m":
                sqlMeasurement = "\"PriceRate\".\"3d\".\"priceRate3m\"";
                measurement = "priceRate3m";
                break;
            case "kline_5m":
                sqlMeasurement = "\"PriceRate\".\"5d\".\"priceRate5m\"";
                measurement = "priceRate5m";
                break;
            case "kline_10m":
                sqlMeasurement = "\"PriceRate\".\"10d\".\"priceRate10m\"";
                measurement = "priceRate10m";
                break;
            case "kline_15m":
                sqlMeasurement = "\"PriceRate\".\"15d\".\"priceRate15m\"";
                measurement = "priceRate15m";
                break;
            case "kline_30m":
                sqlMeasurement = "\"PriceRate\".\"30d\".\"priceRate30m\"";
                measurement = "priceRate30m";
                break;
            case "kline_1h":
                sqlMeasurement = "\"PriceRate\".\"60d\".\"priceRate1h\"";
                measurement = "priceRate1h";
                break;
            case "kline_2h":
                sqlMeasurement = "\"PriceRate\".\"120d\".\"priceRate2h\"";
                measurement = "priceRate2h";
                break;
            case "kline_6h":
                sqlMeasurement = "\"PriceRate\".\"360d\".\"priceRate6h\"";
                measurement = "priceRate6h";
                break;
            case "kline_12h":
                sqlMeasurement = "\"PriceRate\".\"720d\".\"priceRate12h\"";
                measurement = "priceRate12h";
                break;
            case "kline_1D":
            case "kline_1d":
                sqlMeasurement = "\"PriceRate\".\"1dINF\".\"priceRate1d\"";
                measurement = "priceRate1d";
                break;
            case "kline_3D":
            case "kline_3d":
                sqlMeasurement = "\"PriceRate\".\"3dINF\".\"priceRate3d\"";
                measurement = "priceRate3d";
                break;
            case "kline_1W":
            case "kline_1w":
                sqlMeasurement = "\"PriceRate\".\"1wINF\".\"priceRate1w\"";
                measurement = "priceRate1w";
                break;
            default:
                measurement = null;
        }
        if (measurement == null) {
            return new ArrayList<>();
        }
        List<PriceVo> priceVos = influxResultExt.toPOJO(influxDbMapper.query(String.format(sql, sqlMeasurement, startTime, endTime, symbol, unit)), PriceVo.class, measurement);
        return priceVos;
    }

    /**
     * 根据币种名称查询近七天收盘价
     *
     * @param coinName
     * @return
     */
    @Override
    public List<RiseVo> getResiFor7dByCoinName(String coinName) {
        return marketCapManagement.getMarketCapsFor7d(coinName);
    }

    /**
     * 根据交易对查询近七天收盘价
     *
     * @param onlyKey
     * @return
     */
    @Override
    public List<RiseVo> getResiFor7dByOnlyKey(String onlyKey) {
        return marketCapManagement.getMarketCapsFor7d(onlyKey);
    }

    private List<TradeVO> getTradesForTime(String onlyKey, Long startTime, Long endTime) {
        String[] keys = onlyKey.split("_");
        String exchangeName = keys[0];
        String symbolName = keys[1];
        String unitName = keys[2];
        String querySql = "select  *  from \"TopCoinDB\".\"autogen\".\"trade\" WHERE \"exchange\"='%s' AND \"symbol\"='%s' AND \"unit\"='%s' AND \"time\" >= %s AND \"time\" < %s ";
        List<TradeDB> tradeDBS = new InfluxDBResultMapper().toPOJO(influxDbMapper.queryForNanoseconds(String.format(querySql, exchangeName, symbolName, unitName, startTime, endTime)), TradeDB.class);
        return tradeDBS.stream().map(tradeDB -> getTradeVOByTradeDB(tradeDB)).collect(Collectors.toList());
    }

    /**
     * 根据传入的Exchange 进行逗号分隔，
     * 分隔后进行SQL 的拼接
     *
     * @param exchange
     * @return
     */
    private String getExchangeSelcetStr(String exchange, String coinName, String unitName) {
        String[] exchangeArrays = exchange.split(",");
        List<String> exchanges = Arrays.asList(exchangeArrays);
        String str = "\"exchange\"='%s'";
        String collect = exchanges.stream().filter(Objects::nonNull).map(ex -> String.format(str, ex)).collect(Collectors.joining(" OR "));
        StringBuilder stringBuilder = new StringBuilder(" ( ");
        stringBuilder.append(collect);
        stringBuilder.append(" ) ");
        if (coinName != null) {
            stringBuilder.append("AND \"symbol\"= '").append(coinName).append("' ");
        }
        if (unitName != null) {
            stringBuilder.append("AND \"unit\"= '").append(unitName).append("' ");
        }
        return stringBuilder.toString();
    }

    private <T> List getKlineList(QueryResult queryResult, String timeType) {
        return influxResultExt.toPOJO(queryResult, KlineOthers.class, timeType);
    }

    /**
     * K线的设置
     * <p>
     */
    @Data
    public class KeyLinData {
        String libraryName;//库名

        String strategyName;//策略名

        /**
         * 跟随谁的设置 例如 分钟小时的跟随分钟 日K线以上的跟随日K
         */
        String settingFrom;

        //每次获取的最小时间戳
        Long minTime;

        public KeyLinData(String libraryName, String strategyName, String settingFrom, Long minTime) {
            this.libraryName = libraryName;
            this.strategyName = strategyName;
            this.settingFrom = settingFrom;
            this.minTime = minTime;
        }
    }
}