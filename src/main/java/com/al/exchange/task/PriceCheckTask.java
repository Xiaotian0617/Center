package com.al.exchange.task;

import com.al.exchange.dao.domain.MarketCache;
import com.al.exchange.dao.domain.MarketSourceType;
import com.al.exchange.service.ExchangeRealtimeMarketService;
import com.al.exchange.util.CalculatePriceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 价格检测
 */
@Slf4j
@Component
public class PriceCheckTask {
    @Autowired
    ExchangeRealtimeMarketService exchangeRealtimeMarketService;


    /**
     * 检测是否正常的map信息
     */
    private static final Map<String, MarketSourceType> marketCheckResultMap = new HashMap<>();


    /**
     * NOTE ：
     * 检查交易所返回的金额是否有错误，如果价格波动大于90%或者一分钟内没有返回最新价格
     * 价格波动是与上一条记录判断
     * 如果不满足就认为有误
     * 有误的话，更改全局变量推送第三方平台抓取的数
     */
    //@Scheduled(cron = "${scheduled.exchangetimes}")
    public void priceCheckByExchange() {
        log.debug("开始校验交易所返回金额是否正确");
        try {
            List<MarketCache> markets = getAllMarketByExchange();
            marketsCheckLastPrice(markets);
        } catch (Throwable e) {
            log.error("系统校验交易所返回market是否正确出现异常，请检查", e);
        }
    }

    /**
     * 市场信息的校验并返回校验结果
     * 判断公式为：
     * 新来的数据减去上一次的数据，然后除以上一次的数据 如果大于0.9 则标识可能有误，需查看第三方数据
     *
     * @param markets
     */
    private void marketsCheckLastPrice(List<MarketCache> markets) {
        markets.stream().filter(MarketCache::isReady).forEach(marketCache -> {
            if (!isaBoolean(marketCache)) {
                log.trace("{}币种校验完成，来源为{}", marketCache.now().getOnlyKey(), MarketSourceType.Exchange);
                marketCheckResultMap.put(marketCache.now().getOnlyKey(), MarketSourceType.Exchange);
            } else {
                marketCheckResultMap.remove(marketCache.now().getOnlyKey());
            }
        });
    }

    /**
     * 交易所1分钟内判断错误规则
     * 新时间的最后值和旧时间的最后值得差除以旧时间的最后值的比例如果超过90% 或者他们的之间的时间差大于60秒
     *
     * @param listMarket
     * @return
     */
    private boolean isaBoolean(MarketCache listMarket) {
        return CalculatePriceUtils.calculatePriceChange(listMarket.last().getLast(), listMarket.now().getLast())
                .compareTo(new BigDecimal("90")) > -1 || listMarket.now().getTimestamp() - listMarket.last().getTimestamp() > 60000L;
    }


    /**
     * 查询最新的市场信息
     *
     * @return
     */
    private List<MarketCache> getAllMarketByExchange() {
        return exchangeRealtimeMarketService.getNowMarkets();
    }

    /**
     * NOTE :
     * 检查第三方平台价格是否有误
     */
    //@Scheduled(cron = "${scheduled.thirdtimes}")
    public void priceCheckByQuintar() {
        log.debug("开始校验金塔平台返回金额是否正确");
        List<MarketCache> markets = getAllMarketByThird(MarketSourceType.Quintar);
        if (markets == null || markets.size() == 0) {
            log.error("Quintar尚无数据进入校验");
            return;
        }
        //NOTE: 传入Mytoken是因为从Mytoken开始进行检查
        spotCheck(markets, MarketSourceType.Mytoken);
    }

    /**
     * NOTE :
     * 检查第三方平台价格是否有误
     */
    //@Scheduled(cron = "${scheduled.thirdtimes}")
    public void priceCheckByAlCoin() {
        log.debug("开始校验AlCoin平台返回金额是否正确");
        List<MarketCache> markets = getAllMarketByThird(MarketSourceType.Alcoin);
        if (markets == null || markets.size() == 0) {
            log.error("AiCoin尚无数据进入校验");
            return;
        }
        //NOTE: 传入Mytoken是因为从Mytoken开始进行检查
        spotCheck(markets, MarketSourceType.Mytoken);
    }

    /**
     * NOTE :
     * 检查第三方平台价格是否有误
     */
    //@Scheduled(cron = "${scheduled.thirdtimes}")
    public void priceCheckByMyToken() {
        log.debug("开始校验MyToken平台返回金额是否正确");
        List<MarketCache> markets = getAllMarketByThird(MarketSourceType.Mytoken);
        if (markets == null || markets.size() == 0) {
            log.error("MyToken尚无数据进入校验");
            return;
        }
        //NOTE: 传入Mytoken是因为从Mytoken开始进行检查
        spotCheck(markets, MarketSourceType.Mytoken);
    }

    /**
     * 第三方平台抽样调查部分
     *
     * @param markets
     */
    private void spotCheck(List<MarketCache> markets, MarketSourceType marketSourceType) {
        List<MarketCache> checkMarkets = markets.stream().filter(marketCache -> {
            return marketCache.isReady() && "Bitfinex_BTC_USDT,Okex_BTC_USDT,Bitstamps_BTC_USDT".contains(marketCache.getOnlyKey().onlyKey());
        }).collect(Collectors.toList());
        if (checkMarkets.size() == 0) {
            return;
        }
        Map<String, Boolean> checkResult = new HashMap<>();
        checkMarkets.forEach(marketCache -> checkResult.put(marketCache.getOnlyKey().onlyKey(), false));
        switch (marketSourceType) {
            case Other:
                break;
            default:
                break;
        }
    }

    /**
     * 增加Map返回结果根据第三方获取结果
     * 增加逻辑：
     * 遇到相同的不覆盖
     *
     * @param markets
     * @param marketSourceType
     */
    private void addMapResultInfoByThird(List<MarketCache> markets, Map<String, Boolean> checkResult, MarketSourceType marketSourceType) {
        markets.forEach(marketCache -> {
            if (checkResult.get(marketCache.getOnlyKey()) != null &&
                    !checkResult.get(marketCache.getOnlyKey()) &&
                    marketCheckResultMap.get(marketCache.getOnlyKey()) == null) {
                log.trace("{}币种校验完成，来源为{}", marketCache.now().getOnlyKey(), marketSourceType);
                marketCheckResultMap.put(marketCache.now().getOnlyKey(), marketSourceType);
                checkResult.put(marketCache.now().getOnlyKey(), true);
            }
        });
    }

    /**
     * 判断第三方平台价格是否有误  判断标准部分
     * 有误的标准为：
     * 1.Bitfinex的BTC对USDT的价格可以抓取到
     * 2.Okex的BTC对USDT的价格可以抓取到
     * 3.Bitstamps的BTC对USDT的价格可以抓取到
     *
     * @param markets
     * @return
     */
    private boolean existPrice(List<MarketCache> markets) {
        Iterator<MarketCache> iterator = markets.iterator();
        while (iterator.hasNext()) {
            MarketCache market = iterator.next();
            if (market.now() != null &&
                    market.now().getLast().compareTo(BigDecimal.ZERO) != 0) {
                log.debug("第三方平台：OnlyKey:" + market.getOnlyKey() +
                        ",Time:" + market.now() + ",Last:" + market.now().getLast());
                return true;
            }
        }
        return false;
    }

    /**
     * 从蜘蛛缓存map中拿到map集合
     *
     * @param marketSourceType 平台枚举对象
     * @return
     */
    private List<MarketCache> getAllMarketByThird(MarketSourceType marketSourceType) {
        switch (marketSourceType) {
            default:
                return null;
        }
    }

    public Map<String, MarketSourceType> marketCheckResultMap() {
        return marketCheckResultMap;
    }
}
