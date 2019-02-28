package com.al.exchange.task;

import com.al.exchange.dao.domain.*;
import com.al.exchange.service.DataProvideService;
import com.al.exchange.service.ExchangeRealtimeMarketService;
import com.al.exchange.service.management.*;
import com.al.exchange.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TOPCOIN
 * file:topcoin
 * <p>
 *
 * @author mr.wang
 * @version 2018年02月01日11:03:37 V1.0
 * @par 版权信息：
 * 2018 copyright 河南艾鹿网络科技有限公司 all rights reserved.
 */
@Slf4j
@Component
public class MarketInfoEnhanceTask {


    @Autowired
    ExchangeRealtimeMarketService exchangeRealtimeMarketService;

    @Autowired
    DataProvideService dataProvideService;

    @Autowired
    MarketCapManagement marketCapManagement;

    @Autowired
    InfluxDbMapper influxDbMapper;

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    @Autowired
    PriceChangeManagement priceChangeManagement;
    @Autowired
    HighLowChangeManagement highLowChangeManagement;

    @Autowired
    AmountChangeManagement amountChangeManagement;

    @Autowired
    MarketValueEnhanceTask marketValueEnhanceTask;

    @Autowired
    OperationFileUtils operationFileUtils;

    @Autowired
    InfluxResultExt influxResultExt;

    @Autowired
    PriceRateTask priceRateTask;

    @Autowired
    ExchangeBookTask exchangeBookTask;


    /**
     * 市场Map的最终推送数据(每一秒更新一次)
     */
    private final static Map<String, MarketVO> marketMap = new ConcurrentHashMap<>();

    /**
     * 价格波动交易所最新map
     */
    private final static Map<String, Market> exchangeMarketMap = new ConcurrentHashMap<>();

    /**
     * 价格波动金塔最新map
     */
    private final static Map<String, Market> quintarMarketMap = new ConcurrentHashMap<>();


    /**
     * 价格波动alCoin最新map
     */
    private final static Map<String, Market> aicoinMarketMap = new ConcurrentHashMap<>();


    /**
     * 价格波动myToken最新map
     */
    private final static Map<String, Market> myTokenMarketMap = new ConcurrentHashMap<>();

    /**
     * 当前价格的前24小时前的最后一条Market信息
     */
    private final static Map<String, PriceChange> last24HourMarket = new ConcurrentHashMap<>();

    /**
     * 当前价格的当日00:00时的最后一条Market信息
     */
    private final static Map<String, PriceChange> lastZeroTimeMarket = new ConcurrentHashMap<>();

    /**
     * key1:exchange
     * key2:symbol
     */
    private final static Map<String,Map<String,List<VolumeVo>>> volumesFor30Day = new ConcurrentHashMap<>();

//    /**
//     * 截止当前OnlyKey所对应的涨跌幅等增强信息
//     */
//    public final static Map<String,PriceChange> priceChangeMap = new HashMap<>();

    //币种市值排名Map
    private final static Map<String, Integer> capRankMap = new HashMap<>();

    public static Map<String, List<VolumeVo>> getVolumesFor30Day(String exchangeName) {
        return volumesFor30Day.get(exchangeName);
    }

    //@Scheduled(fixedRate = 3600 * 1000)
    @org.springframework.context.event.EventListener(value = ApplicationReadyEvent.class)
    @Order(1)
    void init() {
        try {
            log.info("数据中心开始初始化必备数据！");
            //初始化市值信息 同时初始化OnlyKeySet 并按照要求排序 以供后续排序查询等信息
            int keyNum = 0;
            List<MarketKey> onlyKeyList = new ArrayList<>();
            initOnlyKey(keyNum, onlyKeyList);
            if (onlyKeyList.size() == 0) {
                log.error("OnlyKey初始化失败，无法进行下一步初始化，系统启动失败！");
                return;
            }
            addOnlyKeyAfterComparator(onlyKeyList);
            //初始化priceChangeMap
            if (!priceChangeManagement.isInit()) {
                priceChangeManagement.init();
            }
            int marketNum = 0;//为递归跳出使用
            //初始化行情map
            log.info("开始初始化行情信息");
            initMarket(marketNum);
            log.info("开始初始化交易所列表");
            exchangeBookTask.loadExchangeBook();
            //开始调用其计算涨跌幅
            log.info("开始从Redios获取最新的汇率信息");
            priceRateTask.getNewPriceRateFromRedis();
            log.info("开始初始化涨跌幅信息");
            coverNoVolDayKline();
            calcPriceChange();
            log.info("数据中心初始化必备数据完成！");
            //marketValueEnhanceTask.calculateamount();
        } catch (Throwable e) {
            log.error("系统初始化出现异常，请检查", e);
        }
    }

    /**
     * 计算涨跌幅
     */
    public void calcPriceChange() {
        get30DayVolumeByExchangeAndSymbol();
        getLast1HourMarketByTime();
        getLast7DayMarketByTime();
        getLast24HourMarketByTime();
        getLast30DayMarketByTime();
        getLastEightTimeMarketByTime();
        getLastZeroTimeMarketByTime();
        calculate24HourHighLow();
        calculate24hourvolume();
        marketValueEnhanceTask.getLast1HourMarketByTime();
        marketValueEnhanceTask.getLast7DayMarketByTime();
        marketValueEnhanceTask.getLast24HourMarketByTime();
        marketValueEnhanceTask.getLast30DayMarketByTime();
        marketValueEnhanceTask.getLastEightTimeMarketByTime();
        marketValueEnhanceTask.getLastZeroTimeMarketByTime();
        marketValueEnhanceTask.calculate24HourHighLow();
    }

    private void initOnlyKey(int num, List<MarketKey> onlyKeyList) {
        log.info("开始初始化OnlyKey信息");
        List<MarketKey> allOnlyKeysFromDB = dataProvideService.getAllOnlyKeysFromDB();
        if (allOnlyKeysFromDB==null||allOnlyKeysFromDB.isEmpty()){
            log.warn("库中没有可供初始化onlykey的数据，改为从文件读取！");
            allOnlyKeysFromDB = dataProvideService.getAllOnlyKeysFromFile();
        }
        onlyKeyList.addAll(allOnlyKeysFromDB);
    }

    private void initMarket(int num) {
        try {
            //如果文件中无数据 就从数据库中查询
            List<MarketDB> list = dataProvideService.getAllLastMarket();
            if (list != null && list.size() != 0) {
                log.trace("所有市场最新价{}", list);
                list.stream().filter(Objects::nonNull).filter(marketDB -> !Objects.equals(null,marketDB.getOnlyKey())).forEach((MarketDB marketKeys) -> {
                    MarketVO marketVO = marketDBToMarket(marketKeys);
                    log.trace("市价 VO{}", marketVO);
                    marketMap.put(marketKeys.getOnlyKey(), marketVO);
                });
                log.warn("行情数据文件初始化失败！但已数据库中初始化！");
                return;
            }
            log.error("行情从数据库中初始化失败！");
        } catch (Throwable e) {
            log.error("初始化行情出错，错误日志为：", e);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //initMarket(++num);
    }

    /**
     * 根据一定规则对onlyKey进行排序
     * /**
     * NOTE:
     * 排序规则
     * 先按照市值排序（手机端保存一个市值排序表，每天更新一次），然后同一个币，有多个货币单位，按照以下规则排序
     * USD-RMB-韩元-日元-欧元-USDT-BTC-ETH-LTC-EOS-ETC-BIGONE,其他币种和单位显示在最后
     * 如果还有其他没有说到的货币单位，优先就就拍最后，按照首字母排序
     * 2018年02月08日16:06:18
     *
     * @return
     */
    private void addOnlyKeyAfterComparator(List<MarketKey> list) {
        //从市值Map中找到相应币种的市值并排序
        //2018年03月08日13:13:05 初始化市值获取改为从文件中读取
        log.info("开始初始化市值信息，同时对Onlykey Set并排序");
        int num = 0;//为递归跳出使用
        initMarketCap(num);
        marketCapManagement.getMarketCaps().forEach(marketCap -> capRankMap.putIfAbsent(marketCap.getSymbol(), marketCap.getRank()));
        if (capRankMap.size() == 0) {
            //如果获取失败   就临时用库中查询的Key为准
            onlyKeyManagement.init(list);
            return;
        }
        try {
            log.info("开始进行OnlyKey排序");
            onlyKeyManagement.init(list);
            log.info("OnlyKey排序成功");
        } catch (Exception e) {
            log.warn("onlykey sort fail", e);
            onlyKeyManagement.init(list);
        }
    }

    private void initMarketCap(int num) {
        List<MarketCap> list = dataProvideService.getAllMarketCaps();
        if (list.size() != 0) {
            marketCapManagement.refresh(list);
            log.info("系统从数据库中成功初始化市值信息！");
            return;
        }
    }

//    private static List<String> sortedUnit = Arrays.asList("BIGONE", "ETC", "EOS", "LTC", "ETH", "BTC", "USDT", "EUR", "JPY", "KRW", "CNY", "USD");
//    private static List<String> sortedType = Arrays.asList("QUARTER", "NEXTWEEK", "THISWEEK");

    @Scheduled(cron = "${scheduled.ayncMarketCap}")
    private void asycMarketCap(){
        try {
            onlyKeyManagement.initSettingByMysql();
            List<MarketCap> list = dataProvideService.getAllMarketCaps();
            if (list.size() != 0) {
                marketCapManagement.refresh(list);
                log.info("系统更新成功市值信息！");
                return;
            }
        }catch (Throwable e){
            log.error("系统更新市值信息和设置出错！",e);
        }
    }


    static Pattern okexPattern = Pattern.compile("(.*)(THISWEEK|NEXTWEEK|QUARTER)$");

    //主是是为了处理OKEX的合约期货的排序
    private String okex(MarketKey key, int group) {
        String symbol = key.symbol();
        Matcher matcher = okexPattern.matcher(symbol);
        return matcher.find() ? matcher.group(group) : symbol;
    }

    private String symbol(MarketKey key) {
        return okex(key, 1);
    }

    private String type(MarketKey key) {
        return okex(key, 2);
    }

    private MarketVO marketDBToMarket(MarketDB marketKeys) {
        return new MarketVO(marketKeys);
    }

    /**
     * 根据交易所信息返回
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void getLast24HourMarketByTime() {
        log.info("开始维护24小时交易所返回Market信息");
        //Map<String, PriceChange> priceChangeMap = operationFileUtils.getLast24HourMarketFromFile();
        //if (priceChangeMap == null || priceChangeMap.size() == 0) {
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        //TODO 从小时K中获取，暂时不用
        //String queryString = "select * from \"Kline1h\".\"60d\".\"no_vol_kline_1h\"  WHERE time > now() - 24h GROUP BY *";
        String queryString = "select * from \"TopCoinDB\".\"autogen\".\"no_vol_kline_1h\"  WHERE time >= now() - 1d group by *  limit 1";
        this.calculationPriceChange(queryString, "no_vol_kline_1h", "24Hour");
    }



    /**
     * 30天交易量数据
     */
    @Scheduled(cron = "${scheduled.calc30DayVolume}")
    void get30DayVolumeByExchangeAndSymbol() {
        log.info("开始计算30天交易量");
        String queryString = "SELECT sum(\"volume\") AS \"volume\" FROM \"TopCoinDB\".\"autogen\".\"kline_1D\" WHERE time > now() - 30d GROUP BY \"exchange\", \"symbol\"";
        QueryResult query = influxDbMapper.query(queryString);
        if (query==null){
            log.warn(" 30天交易量数据为空 ，线上量比榜可能会受影响！");
            return;
        }
        List<VolumeVo> klineOthers = influxResultExt.toPOJO(query, VolumeVo.class, "kline_1D");
        if (klineOthers == null || klineOthers.size() == 0) {
            log.warn(" 30天交易量数据解析为空 ，线上量比榜可能会受影响！");
            return;
        }
        Map<String,Map<String,List<VolumeVo>>> map = new HashMap<>(20000);
        Map<String, List<VolumeVo>> collect = klineOthers.stream()
                .collect(Collectors.groupingBy(VolumeVo::getExchange, Collectors.toList()));
        collect.entrySet().forEach(entry->{
            Map<String, List<VolumeVo>> volumes = entry.getValue().stream().collect(Collectors.groupingBy(VolumeVo::getSymbol, Collectors.toList()));
            if (volumes.isEmpty()){
                return;
            }
            map.put(entry.getKey(),volumes);
        });
        volumesFor30Day.putAll(map);
        log.info("计算30天交易量结束");
    }

    /**
     * 当天0点左右的最近一条数据 (每日凌晨执行).
     */
    @Scheduled(cron = "${scheduled.lastzerotimemarket}")
    void getLastZeroTimeMarketByTime() {
        log.info("开始获取当日凌晨时的Market信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        long zeroTime = DateUtils.getZeroTime().getTime() * 1000000;
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        String queryString = "select * from \"TopCoinDB\".\"autogen\".\"no_vol_kline_1h\" where time = " + zeroTime + " group by *  limit 1";
        this.calculationPriceChange(queryString, "no_vol_kline_1h", "ZeroTime");
    }

    /**
     * 获取距当前时间一小时内的数据（一分钟执行一次）.
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void getLast1HourMarketByTime() {
        log.info("开始获取一小时前的Market信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        //TODO 从小时K中获取，暂时不用
        //String queryString = "select * from \"Kline1h\".\"60d\".\"no_vol_kline_1h\"  WHERE time > now() - 1h GROUP BY *";
        String queryString = "select * from \"TopCoinDB\".\"autogen\".\"no_vol_kline_1h\"  WHERE time > now() - 3h GROUP BY *  limit 1";
        this.calculationPriceChange(queryString, "no_vol_kline_1h", "1Hour");
    }

    /**
     * 获取当日八点的数据（每日八点半执行）.
     */
    @Scheduled(cron = "${scheduled.lasteighttimemarket}")
    void getLastEightTimeMarketByTime() {
        log.info("开始获取当日8点的Market信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        long eightTime = DateUtils.getEightTime().getTime() * 1000000;
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        String queryString = "select * from \"TopCoinDB\".\"autogen\".\"no_vol_kline_1h\"  where time =" + eightTime + " group by *  limit 1";
        this.calculationPriceChange(queryString, "no_vol_kline_1h", "EightTime");
    }

    /**
     * 获取距当前时间7天的数据（一分钟执行一次）.
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void getLast7DayMarketByTime() {
        log.info("开始获取7天前的Market信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        String queryString = "select * from \"TopCoinDB\".\"autogen\".\"no_vol_kline_7D\"  WHERE time >= now() - 7d  GROUP BY *  limit 1";
        this.calculationPriceChange(queryString, "no_vol_kline_7D", "7Day");
    }

    /**
     * 获取距当前时间30天的数据（一分钟执行一次）.
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void getLast30DayMarketByTime() {
        log.info("开始获取30天前的Market信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        String queryString = "select * from \"TopCoinDB\".\"autogen\".\"no_vol_kline_7D\"  WHERE time >= now() - 30d GROUP BY * limit 1";
        this.calculationPriceChange(queryString, "no_vol_kline_7D", "30Day");
    }

    private void calculationPriceChange(String queryString, String measurementName, String types) {
        try {
            List<MarketCapExt> klineOthers = influxResultExt.toPOJO(influxDbMapper.query(queryString), MarketCapExt.class, measurementName);
            if (klineOthers == null || klineOthers.size() == 0) {
                log.warn(" {} 类型初始化失败，线上环境涨跌幅可能会受影响！", types);
                return;
            }
            for (MarketCapExt klineOther : klineOthers) {
                priceChangeManagement.setPriceByType(klineOther.getOnlyKey(), types, klineOther.getClose());
            }
        } catch (Throwable e) {
            log.error("系统查询" + types + "类型出现异常，请检查", e);
        }
    }

    /**
     * 获取24小时最高(低)价（）
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void calculate24HourHighLow() {
        log.info("开始获取最高(低)价");
        try {
            String queryString = "SELECT max(\"high\") as high,min(\"low\") as low,last(\"onlyKey\") as onlyKey FROM \"TopCoinDB\".\"autogen\".\"no_vol_kline_12h\" where time >= now() - 1d group by *";
            List<MarketCapExt> klineOthers = influxResultExt.toPOJO(influxDbMapper.query(queryString), MarketCapExt.class, "no_vol_kline_12h");
            if (klineOthers == null || klineOthers.size() == 0) {
                log.warn("获取market高低为空！");
                return;
            }
            for (MarketCapExt klineOther : klineOthers) {
                HighLowChange highLowChange = new HighLowChange(klineOther.getOnlyKey(), klineOther.getHigh(), klineOther.getLow());
                highLowChangeManagement.put(highLowChange.getOnlykey(), highLowChange);
            }
        } catch (Throwable e) {
            log.error("系统获取market高低出现异常，请检查", e);
        }

    }

    @Scheduled(cron = "${scheduled.calculatevolume}")
    void calculateamount() {
        log.info("开始计算成交量");
        try {
            calculate24hourvolume();
        } catch (Throwable e) {
            log.error("系统计算24小时成交量出现异常，请检查", e);
        }

    }

    private void calculate24hourvolume() {
        QueryResult queryResult = influxDbMapper.query("SELECT sum(\"volume\") AS \"volume\", last(\"onlyKey\") AS \"onlyKey\" FROM \"TopCoinDB\".\"autogen\".\"calc_kline_12h\" WHERE time > now() - 1d group by *");
        if (queryResult==null){
            log.warn("获取market量为空！");
            return;
        }
        List<MarketCapExt> volumeChanges = influxResultExt.toPOJO(queryResult, MarketCapExt.class, "calc_kline_12h");
        for (MarketCapExt volumeChange : volumeChanges) {
            amountChangeManagement.put(volumeChange.getOnlyKey(), volumeChange);
        }
    }


    /**
     * 每日凌晨进行无量K线的日K 高，低，量，收盘价格的覆盖.
     */
    @Scheduled(cron = "${scheduled.lastzerotimemarket}")
    void coverNoVolDayKline() {
        log.info("开始获取当日凌晨时的Market信息");
        Map<String, MarketVO> map = new HashMap<>(2000);
        map.putAll(marketMap);
        List<KLinePO> no_vol_kline_1D = map.entrySet().stream().map(Map.Entry::getValue).map(marketVO -> {
            KLinePO kLinePO = new KLinePO();
            kLinePO.setExchange(marketVO.getExchange());
            kLinePO.setSymbol(marketVO.getSymbol());
            kLinePO.setUnit(marketVO.getUnit());
            kLinePO.setMeasurement("no_vol_kline_1D");
            kLinePO.setHigh(marketVO.getHigh());
            kLinePO.setLow(marketVO.getLow());
            kLinePO.setClose(marketVO.getLast());
            kLinePO.setVolume(marketVO.getVolume());
            Date yesterdatEightTime = DateUtils.getYesterdatEightTime();
            kLinePO.setTimestamp(yesterdatEightTime.getTime());
            return kLinePO;
        }).collect(Collectors.toList());
        influxDbMapper.writeBeans(no_vol_kline_1D);
    }

    /**
     * NOTE ：
     * 根据价格波动校验的结果，拼成结果集，每一秒根据结果集中所对应的数据，从不同的地方拿取不同的最新Market信息
     */
    //@Scheduled(fixedDelayString = "${scheduled.finalGetMarket}", initialDelay = 30 * 1000)
    void getMarketMapByPriceResult() {
        log.debug("开始根据校验后的结果获取最终的价格市场信息");
        try {
            resultListToMap();
            if (!priceChangeManagement.isInit()) {
                log.error("价格涨跌幅计算Map为空，推测涨跌幅计算失败或无数据，请检查！");
                return;
            }
            //TODO 注释掉根据校验结果更新行情数据  改为直接根据展示行情结果更新展示数据
            //Map<String, MarketVO> map = new HashMap<>(marketMap);
            onlyKeyManagement.onlyKeys().forEach(key -> {
                //TODO 未做方法处理 待优化
                Market market = exchangeMarketMap.get(key.onlyKey());//FIXME exchangeMarketMap put 引用逻辑未检查
                if (market != null && !market.isUsed()) {
                    MarketVO marketVO = enhanceMarketInfo(market, MarketSourceType.Exchange);
                    marketMap.put(key.onlyKey(), marketVO);
                    market.setUsed(true);
                    return;
                }
                Market thirdMarket = aicoinMarketMap.get(key.onlyKey());//FIXME aicoinMarketMap put 引用逻辑未检查
                if (thirdMarket != null && !thirdMarket.isUsed()) {
                    MarketVO marketVO = enhanceMarketInfo(thirdMarket, MarketSourceType.Alcoin);
                    marketMap.put(key.onlyKey(), marketVO);
                    thirdMarket.setUsed(true);
                    return;
                }
            });
        } catch (Throwable e) {
            log.error("系统拼最后market结果集时出现异常，请检查", e);
        }
    }

    private MarketVO enhanceMarketInfo(Market market, MarketSourceType marketSourceType) {
        MarketVO marketVO = new MarketVO();
        market.setOnlyKey(market.getOnlyKey());
        priceChangeManagement.initIfAbsent(marketVO.getOnlyKey());
        switch (marketSourceType) {
            case Alcoin:
                marketVO = new MarketVO(market);
                marketVO.setFrom(MarketSourceType.Alcoin.name());
                break;
            case Mytoken:
                marketVO = new MarketVO(market);
                marketVO.setFrom(MarketSourceType.Mytoken.name());
                break;
            case Quintar:
                marketVO = new MarketVO(market);
                marketVO.setFrom(MarketSourceType.Quintar.name());
                break;
            case Exchange:
                MarketVO marketVOMap = marketMap.get(market.getOnlyKey());
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(marketVOMap));
                if (jsonObject != null) {
                    JSONObject.parseObject(JSONObject.toJSONString(market)).entrySet()
                            .stream().filter(objectEntry -> objectEntry.getValue() != null).forEach(objectEntry -> jsonObject.put(objectEntry.getKey(), objectEntry.getValue()));
                    marketVO = new MarketVO(jsonObject.toJavaObject(Market.class));
                } else {
                    //FIXME 这个地方几乎不会走这里但是为防止万一nul做了处理，但是下方的处理逻辑待优化
                    marketVO = new MarketVO(market);
                }
                //从第三方平台获取高低量买一卖一的数据
                Market thirdMarket = getThirdMarket(marketVO.getOnlyKey());
                if (thirdMarket != null) {
                    marketVO.setHigh(thirdMarket.getHigh().compareTo(BigDecimal.ZERO) == 0 ? marketVO.getHigh() : thirdMarket.getHigh());
                    marketVO.setLow(thirdMarket.getLow().compareTo(BigDecimal.ZERO) == 0 ? marketVO.getLow() : thirdMarket.getLow());
                    marketVO.setVolume(thirdMarket.getVolume().compareTo(BigDecimal.ZERO) == 0 ? marketVO.getVolume() : thirdMarket.getVolume());
                    marketVO.setAsk(thirdMarket.getAsk().compareTo(BigDecimal.ZERO) == 0 ? marketVO.getAsk() : thirdMarket.getAsk());
                    marketVO.setBid(thirdMarket.getBid().compareTo(BigDecimal.ZERO) == 0 ? marketVO.getBid() : thirdMarket.getBid());
                }
                marketVO.setFrom(MarketSourceType.Exchange.name());
                MarketCap marketCap = marketCapManagement.getMarketCapsBySymbol(marketVO.getSymbol());
                marketVO.setSymName(marketCap == null ? "" : marketCap.getName());
                break;
            case Other:
                break;
            default:
                log.error("在校验赋值时，进入了default，数据来源为：{}", marketSourceType.name());
                break;
        }
        if (priceChangeManagement.get(marketVO.getOnlyKey()) != null) {
            marketVO.setChange(accept(marketVO.getOnlyKey(), priceChangeManagement.get(marketVO.getOnlyKey()).getPriceFor24Hour()));
            marketVO.setChangeForZeroHour(accept(marketVO.getOnlyKey(), priceChangeManagement.get(marketVO.getOnlyKey()).getPriceFor0Hour()));
        } else {
            priceChangeManagement.initIfAbsent(marketVO.getOnlyKey());
        }
        /* marketVOMap.setSendTime(System.currentTimeMillis());*/
        marketVO.setNeedSend(true);
        log.trace("{}放入Map前的信息:{}", marketVO.getOnlyKey(), marketVO.toString());
        return marketVO;
    }

    private Market getThirdMarket(String onlyKey) {
        if (aicoinMarketMap.get(onlyKey) != null) {
            return aicoinMarketMap.get(onlyKey);
        }
        if (myTokenMarketMap.get(onlyKey) != null) {
            return myTokenMarketMap.get(onlyKey);
        }
        if (quintarMarketMap.get(onlyKey) != null) {
            return quintarMarketMap.get(onlyKey);
        }
        return null;
    }

    /**
     * 计算其涨跌幅
     *
     * @param key
     * @param value
     */
    private BigDecimal accept(String key, BigDecimal value) {
        if (priceChangeManagement.get(key) != null && marketMap.size() != 0 && marketMap.get(key) != null) {
            log.trace("{}今日凌晨最近的一条数据为：{}", key, value.toPlainString());
            log.trace("{}最近的一条数据为：{}", key, marketMap.get(key).toString());
            BigDecimal newPrice = marketMap.get(key).getLast() == null ? BigDecimal.ZERO : marketMap.get(key).getLast();
            return CalculatePriceUtils.calculatePriceChange(value, newPrice);
        }
        return BigDecimal.ZERO;
    }

    private void resultListToMap() {
        exchangeListToMap();
    }

    private void exchangeListToMap() {
        exchangeRealtimeMarketService.getNowMarkets().stream().filter(MarketCache::isReady)
                .forEach(marketCache -> checkMarketUsed(marketCache, exchangeMarketMap));
    }

    private void checkMarketUsed(MarketCache marketCache, Map marketMap) {
        Market nowMarket = marketCache.now();
        if (!nowMarket.isUsed()) {
            marketMap.put(marketCache.getOnlyKey().onlyKey(), nowMarket);
            //根据引用 修改传入的对象的为已经拿过了
//            log.trace("{}的数据被更新啦，更新数据为：{}", nowMarket);
        }
    }


    //TODO 重构临时方法
    public Map<String, MarketVO> getMarketMap() {
        return marketMap;
    }

    public Map<String, PriceChange> getLast24HourMarket() {
        return last24HourMarket;
    }

    public Map<String, PriceChange> getLastZeroTimeMarket() {
        return lastZeroTimeMarket;
    }

    public Map<String, Market> getQuintarMarketMap() {
        return quintarMarketMap;
    }

    public Map<String, Market> getExchangeMarketMap() {
        return exchangeMarketMap;
    }

    public Map<String, Market> getAicoinMarketMap() {
        return aicoinMarketMap;
    }

    public Map<String, Market> getMyTokenMarketMap() {
        return myTokenMarketMap;
    }

    public static void main(String[] args) {
        ArrayList<Integer> objects = Lists.newArrayList(1, 2, 2, 3, 3, 4, 5, 6);
        List<Integer> sorted = objects.stream().sorted(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int i = o1 - o2;
                if (i == 0) {
                    i = -1;
                }
                return i;
            }
        }).collect(Collectors.toList());
        for (Integer object : sorted) {
            System.out.println(object);
        }
    }
}
