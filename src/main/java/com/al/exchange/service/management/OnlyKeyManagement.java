package com.al.exchange.service.management;

import com.al.exchange.dao.domain.*;
import com.al.exchange.dao.mapper.InformationOwnMapper;
import com.al.exchange.dao.mapper.OnlyKeysConfMapper;
import com.al.exchange.dao.mapper.OnlykeyBeanMapper;
import com.al.exchange.service.DataProvideService;
import com.al.exchange.util.InfluxDbMapper;
import com.al.exchange.util.redis.ObjectRedisService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.list.SetUniqueList;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 09/02/2018 12:01
 */
@Component
@Slf4j
public class OnlyKeyManagement {

    @Resource
    private OnlykeyBeanMapper onlykeyMapper;
    @Autowired
    private InfluxDbMapper influxDbMapper;
    @Resource
    private InformationOwnMapper informationOwnMapper;

    @Resource
    SourceSettingManagement sourceSettingManagement;

    @Resource
    ObjectRedisService objectRedisService;

    private static int mitute = 60;

    @Value("${onlyKey.update-frequency}")
    private int updateFrequency = 60 * 60 * 1000;

    @Autowired
    OnlyKeysConfMapper onlyKeysConfMapper;

    @Autowired
    DataProvideService dataProvideService;
    /**
     * 库中onlyKey的最终推送数据（每小时更新一次）
     * 如果库中我们没有这个交易所，但我们会先把OnlyKey弄出来，以动态更新OnlyKey
     */
    private final static SetUniqueList<MarketKey> onlyKeyMap = SetUniqueList.setUniqueList(new ArrayList<>());
    private final static SetUniqueList<Exchange> exchangeMap = SetUniqueList.setUniqueList(new ArrayList<>());
    private final static HashMap<String, SetUniqueList<MarketKey>> symbolIndex = new HashMap<>();
    private final static HashMap<String, SetUniqueList<MarketKey>> unitIndex = new HashMap<>();
    private final static HashMap<String, SetUniqueList<MarketKey>> exchangeIndex = new HashMap<>();
    /**
     * Key coinMarketCap Name
     * Value  nameOwn
     */
    private final static HashMap<String, String> ownNamesMap = new HashMap<>();

    /**
     * Value  nameOwn
     * Key coinMarketCap Name
     */
    private final static HashMap<String, String> coinMarketCapNameMap = new HashMap<>();

    /**
     * Key  nameOwn
     * Value  InformationOwnDto
     */
    private final static HashMap<String, InformationOwnDto> informationOwnDtoHashMap = new HashMap<>();

    public HashMap<String, InformationOwnDto> getInformationOwnDtoHashMap() {
        return informationOwnDtoHashMap;
    }

    public HashMap<String, String> getCoinMarketCapNameMap() {
        return coinMarketCapNameMap;
    }

    public String getCoinMarketCapNameMapByName(String ownName) {
        return coinMarketCapNameMap.get(ownName);
    }

    public  Map<String, OnlyKeysConf> onlyKeysConfMap = Maps.newHashMap();
    //key:own name
    public  Map<String, InformationOwnDto> informationOwns = Maps.newHashMap();

    public Map<String, OnlyKeysConf> getOnlyKeysConfMap() {
        return onlyKeysConfMap;
    }

    public OnlyKeysConf getOnlyKeysConfMap(String key) {
        return onlyKeysConfMap.get(key);
    }

    public Map<String, InformationOwnDto> getInformationOwns() {
        return informationOwns;
    }

    ReentrantLock refreshLock = new ReentrantLock();


    String exportSymbol = "Okex_BTCWEEK_USD,Okex_ETHWEEK_USD,Okex_BTGWEEK_USD,Okex_BCHWEEK_USD,Okex_EOSWEEK_USD,Okex_ETCWEEK_USD,Okex_XRPWEEK_USD";

    public List<MarketKey> onlyKeys() {
        if (refreshLock.isLocked()) {
            throw new RuntimeException("币种正在刷新");
        }
        return Collections.unmodifiableList(new ArrayList<>(onlyKeyMap));
    }

    public List<Exchange> exchanges() {
        if (refreshLock.isLocked()) {
            throw new RuntimeException("币种正在刷新");
        }
        return Collections.unmodifiableList(new ArrayList<>(exchangeMap));
    }

    public void init(List<MarketKey> list) {
        informationOwns = informationOwnMapper.getInformationOwn().stream().collect(Collectors.toMap(InformationOwnDto::getName, Function.identity()));
        List<OnlyKeysConf> onlyKeysConfs = onlyKeysConfMapper.selectByExample(new OnlyKeysConfExample());
        onlyKeysConfMap = onlyKeysConfs.stream().collect(Collectors.toMap(OnlyKeysConf::getOnlyKey, Function.identity()));
        List<MarketKey> marketKeys = list.stream()
                .sorted(this::sortOnlyKey)
                .collect(Collectors.toList());
        refreshLock.lock();
        try {
            for (int i = 0; i < marketKeys.size(); i++) {
                MarketKey marketKey = marketKeys.get(i);
                if (exportSymbol.contains(marketKey.onlyKey())) {
                    //如果查询出的币种是上方定义的币种就不加入系统
                    continue;
                }
                onlyKeyMap.add(marketKey);
                initSymbolIndex(marketKey);
                initExchangeIndex(marketKey);
                initUnitIndex(marketKey);
                exchangeMap.add(new Exchange(marketKey.exchange()));
            }
            initSettingByMysql();
        } finally {
            refreshLock.unlock();
        }
    }

    public void initSettingByMysql() {
        initOnlyKeysMap();
        initInformationOwn();
    }

    private static List<String> sortedUnit = Arrays.asList("BIGONE", "ETC", "EOS", "LTC", "ETH", "BTC", "USDT", "EUR", "JPY", "KRW", "CNY", "USD");
    private static List<String> sortedType = Arrays.asList("QUARTER", "NEXTWEEK", "THISWEEK");

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

    private int sortOnlyKey(MarketKey s, MarketKey s1) {

        int sorted = 0;
        boolean sameExchange = Objects.equals(s.exchange(), s1.exchange());
        //相同交易所才进行比较
        if (sameExchange) {
            //getRank(s);
            //按币种市值排序，市值从coinmarketcap获取, 市值不存在的按10000处理
//            sorted = capRankMap.getOrDefault(symbol(s), 10000) - capRankMap.getOrDefault(symbol(s1), 10000);
            sorted = getRank(s) - getRank(s1);
//            //如果币种相同比较单位
            if (sorted == 0) {//rank 一样
                sorted = sortedUnit.indexOf(s1.unit()) - sortedUnit.indexOf(s.unit());
                //币种单位排名都相同。可能是市值中没有的且同一个单位的，也可能是okex的合约，对合约按本周，次周，季度排序。
                if (sorted == 0 && "Okex".equals(s.exchange())) {
                    sorted = sortedType.indexOf(type(s1)) - sortedType.indexOf(type(s));
                }
            }
        }
        return sorted;
    }

    private int getRank(MarketKey s) {
        OnlyKeysConf onlyKeysConf = onlyKeysConfMap.get(s.getOnlyKey());
        if (null==onlyKeysConf){
            return Integer.MAX_VALUE;
        }
        InformationOwnDto informationOwnDto = informationOwns.get(onlyKeysConf.getAllName());
        if (null==informationOwnDto){
            return Integer.MAX_VALUE;
        }
        return informationOwnDto.getRank();
    }

    /**
     * 增加临时的OnlyKey
     * NOTE:
     * 临时的OnlyKey 指数据中心接收到了这些交易对，但是由于运营还未设置这些交易对所以咱不入库的一些交易对
     */
    public static void addTemporaryKeys(String onlyKey) {
        MarketKey marketKey = new MarketKey();
        marketKey.setOnlyKey(onlyKey);
        marketKey.setTime(System.currentTimeMillis() + "");
        marketKey.setNew(true);
        onlyKeyMap.add(marketKey);
    }

    private void initOnlyKeysMap() {
        List<OnlyKeysConf> onlyKeysConfs = onlyKeysConfMapper.selectByExample(new OnlyKeysConfExample());
        onlyKeysConfMap = onlyKeysConfs.stream().collect(Collectors.toMap(OnlyKeysConf::getOnlyKey, Function.identity()));
        List<OnlyKeysMapDto> onlyKeysMapDtoList = onlykeyMapper.getOnlyKeyAndName();
        if (onlyKeysMapDtoList.size() > 0) {
            for (OnlyKeysMapDto onlyKeysMapDto : onlyKeysMapDtoList) {
                resetOnlyKeyconf(onlyKeysMapDto);
            }
        }
        try {
            Map<String, Object> collect = onlyKeysMapDtoList.stream()
                    .filter(onlyKeysMapDto -> StringUtils.hasText(onlyKeysMapDto.getAllName()))
                    .collect(Collectors.toMap(OnlyKeysMapDto::getOnlyKey, OnlyKeysMapDto::getAllName));
            objectRedisService.setHashMap("keyName", collect);
        } catch (Throwable e) {
            log.error("将OnlyKey于全称对应关系放入Redis出错！", e);
        }
        log.info("从Mysql中初始化交易对设置成功！");
    }

    private void initInformationOwn() {
        List<InformationOwnDto> informationOwnDtoList = informationOwnMapper.getInformationOwn();
        informationOwns = informationOwnDtoList.stream().collect(Collectors.toMap(InformationOwnDto::getName, Function.identity()));
        if (informationOwnDtoList.size() > 0) {
            for (InformationOwnDto informationOwnDto : informationOwnDtoList) {
                resetInformationOwn(informationOwnDto);
            }
        }
        try {
            objectRedisService.setHashMap("coinNameToOwnName", informationOwnDtoList.stream().filter(informationOwnDto -> StringUtils.hasText(informationOwnDto.getName()) && StringUtils.hasText(informationOwnDto.getNameSelf())).collect(Collectors.toMap(InformationOwnDto::getNameSelf, InformationOwnDto::getName, (o1, o2) -> o1)));
            objectRedisService.setHashMap("ownNameToCoinName", informationOwnDtoList.stream().filter(informationOwnDto -> StringUtils.hasText(informationOwnDto.getName()) && StringUtils.hasText(informationOwnDto.getNameSelf())).collect(Collectors.toMap(InformationOwnDto::getName, InformationOwnDto::getNameSelf, (o1, o2) -> o1)));
        } catch (Throwable e) {
            log.error("将CoinMarketCap于全称对应关系放入Redis出错！", e);
        }
        log.info("从Mysql中初始化币种设置成功！");
    }

    private void initSymbolIndex(MarketKey marketKey) {
        SetUniqueList<MarketKey> marketKeys = symbolIndex.get(marketKey.symbol());
        if (marketKeys == null) {
            marketKeys = SetUniqueList.setUniqueList(new ArrayList<>());
            symbolIndex.put(marketKey.symbol(), marketKeys);
        }
        marketKeys.add(marketKey);
    }

    private void initExchangeIndex(MarketKey marketKey) {
        SetUniqueList<MarketKey> marketKeys = exchangeIndex.get(marketKey.exchange());
        if (marketKeys == null) {
            marketKeys = SetUniqueList.setUniqueList(new ArrayList<>());
            exchangeIndex.put(marketKey.exchange(), marketKeys);
        }
        marketKeys.add(marketKey);
    }

    private void initUnitIndex(MarketKey marketKey) {
        SetUniqueList<MarketKey> marketKeys = unitIndex.get(marketKey.unit());
        if (marketKeys == null) {
            marketKeys = SetUniqueList.setUniqueList(new ArrayList<>());
            unitIndex.put(marketKey.unit(), marketKeys);
        }
        marketKeys.add(marketKey);
    }

    public List<MarketKey> getOnlyKeysBySymbol(String symbol) {
        if (refreshLock.isLocked()) {
            throw new RuntimeException("币种正在初始化");
        }
        List<MarketKey> marketKeys = symbolIndex.get(symbol);
        if (marketKeys == null) {
            return Collections.unmodifiableList(new ArrayList<>());
        }
        return Collections.unmodifiableList(new ArrayList<>(marketKeys.stream().filter(marketKey -> !marketKey.isNew()).collect(Collectors.toList())));
    }

    public List<MarketKey> getOnlyKeysByExchange(String exchange) {
        if (refreshLock.isLocked()) {
            throw new RuntimeException("币种正在初始化");
        }
        List<MarketKey> marketKeys = exchangeIndex.get(exchange);
        if (marketKeys == null) {
            return Collections.unmodifiableList(new ArrayList<>());
        }
        return Collections.unmodifiableList(new ArrayList<>(marketKeys.stream().filter(marketKey -> !marketKey.isNew()).collect(Collectors.toList())));
    }

    protected ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    PeriodicTrigger periodicTrigger = new PeriodicTrigger(updateFrequency, TimeUnit.MILLISECONDS);

    @EventListener(ApplicationReadyEvent.class)
    public void startMaintainOnlykey() {
        periodicTrigger.setInitialDelay(60 * 60 * 1000);
        scheduler.initialize();
        scheduler.schedule(() -> {
            try {
                Set<MarketKey> marketKeys = refreshOnlykeys(mitute);
                init(Lists.newArrayList(marketKeys));
            } catch (Exception e) {
                log.error("增量更新 onlykey 出错", e);
            }
        }, periodicTrigger);

    }

    //
    public Set<MarketKey> refreshOnlykeys(int mitute) {
        String queryMarketSql = "SELECT onlyKey  FROM market WHERE time > now()- " + mitute + "m GROUP BY  *   limit 1";
        QueryResult queryMarketResult = influxDbMapper.query(queryMarketSql);
        List<MarketKey> marketKeys = new InfluxDBResultMapper().toPOJO(queryMarketResult, MarketKey.class);
        onlyKeyMap.addAll(marketKeys);
        return onlyKeyMap.asSet();
    }

    //
    public String getOwnNamesMap(String key) {
        return ownNamesMap.get(key);
    }

//    public String getcoinMarketCapNameMap(String key) {
//        return coinMarketCapNameMap.get(key);
//    }

    public void resetOnlyKeyconf(OnlyKeysMapDto onlyKeysMapDto) {
        SourceSettingManagement.OnlykeySetted onlykeySetted = sourceSettingManagement.new OnlykeySetted(onlyKeysMapDto);
        if (onlyKeysMapDto.getDKlineConf() != null) {
            onlykeySetted.setDayKlineSetted(sourceSettingManagement.new MarketDayKlineSetted(onlyKeysMapDto.getDKlineConf()));
        }
        if (onlyKeysMapDto.getHighLowConf() != null) {
            onlykeySetted.setHighLowSetted(sourceSettingManagement.new MarketHighLowSetted(onlyKeysMapDto.getHighLowConf()));
        }
        if (onlyKeysMapDto.getPriceConf() != null) {
            onlykeySetted.setLastPriceSetted(sourceSettingManagement.new MarketLastPriceSetted(onlyKeysMapDto.getPriceConf()));
        }
        if (onlyKeysMapDto.getMKlineConf() != null) {
            onlykeySetted.setMinuteKlineSetted(sourceSettingManagement.new MarketMinuteKlineSetted(onlyKeysMapDto.getMKlineConf()));
        }
        if (onlyKeysMapDto.getVolConf() != null) {
            onlykeySetted.setMarketVolSetted(sourceSettingManagement.new MarketVolSetted(onlyKeysMapDto.getVolConf()));
        }
        sourceSettingManagement.setMarketSettedMap(onlyKeysMapDto.getOnlyKey(), onlykeySetted);
    }

    public void resetInformationOwn(InformationOwnDto informationOwnDto) {
        ownNamesMap.put(informationOwnDto.getNameSelf(), informationOwnDto.getName());
        coinMarketCapNameMap.put(informationOwnDto.getName(), informationOwnDto.getNameSelf());
        informationOwnDtoHashMap.put(informationOwnDto.getName(), informationOwnDto);
        SourceSettingManagement.MarketCapSetted marketCapSetted = sourceSettingManagement.new MarketCapSetted();
        List<SourceSettingManagement.MarketCapPriceWeightSetted> priceWeights = new ArrayList<>();
        List<SourceSettingManagement.MarketCapVolSetted> volSetteds = new ArrayList<>();
        if (StringUtils.hasText(informationOwnDto.getPriceFrom())) {
            String[] priceFroms = informationOwnDto.getPriceFrom().split(";");
            for (int i = 0; i < priceFroms.length; i++) {
                String[] priceFromStr = priceFroms[i].split(",");
                String onlyKey = priceFromStr[0];
                BigDecimal weight = new BigDecimal(Integer.parseInt(priceFromStr[1]));
                priceWeights.add(sourceSettingManagement.new MarketCapPriceWeightSetted(onlyKey, weight));
            }
            marketCapSetted.setPriceWeights(priceWeights);
        }
        if (StringUtils.hasText(informationOwnDto.getDayVolume())) {
            String[] dayVolumes = informationOwnDto.getDayVolume().split(";");
            for (int i = 0; i < dayVolumes.length; i++) {
                String keys = dayVolumes[i];
                volSetteds.add(sourceSettingManagement.new MarketCapVolSetted(keys));
            }
            marketCapSetted.setVolSetteds(volSetteds);
        }
        sourceSettingManagement.setMarketCapSettedMap(informationOwnDto.getName(), marketCapSetted);
    }
}
