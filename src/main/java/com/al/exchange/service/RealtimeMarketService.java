package com.al.exchange.service;

import com.al.exchange.dao.domain.Market;
import com.al.exchange.dao.domain.MarketCache;
import com.al.exchange.monitor.DataSourceStatusMonitor;
import com.al.exchange.util.InfluxDbMapper;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 维护最近几次market
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 31/01/2018 11:14
 */
@Slf4j
public abstract class RealtimeMarketService implements DataSourceStatusMonitor {
    private final ConcurrentMap<String, MarketCache> exchangeMarkets = Maps.newConcurrentMap();
    private final ConcurrentMap<String, Long> exchangeLastDatatime = Maps.newConcurrentMap();

    //TODO 按 onlykey 加锁
    private final ReentrantLock lock = new ReentrantLock();
    @Autowired
    InfluxDbMapper influxDbMapper;

    public void updateMarket(Market market) {
        if (market == null || !StringUtils.hasText(market.getOnlyKey())) {
            return;
        }
        lock.lock();
        MarketCache marketCache = null;
        try {
            marketCache = exchangeMarkets.get(market.getOnlyKey());
            //接收的行情时间大于缓存行情时间,保存
            if (marketCache == null) {
                marketCache = new MarketCache(market);
                exchangeMarkets.put(market.getOnlyKey(), marketCache);
                cacheMarket(market, marketCache);
                return;
            }
        } catch (Throwable e) {
            log.error("更新行情信息错误！错误Market信息为" + JSON.toJSONString(market), e);
        } finally {
            lock.unlock();
        }

        if (marketCache != null && (!marketCache.isReady()) || (checkDelayOrRepeat(market, marketCache))) {
            cacheMarket(market, marketCache);
        }
    }

    private boolean checkDelayOrRepeat(Market market, MarketCache cache) {
        lock.lock();
        try {
            Market now = cache.now();
            if (market.getLast() == null || market.getLast().equals(BigDecimal.ZERO)) {
                log.debug("价格或量为空{}", market);
                return false;
            }
            if (market.getTimestamp().compareTo(now.getTimestamp()) > 0) {
                //价格未变化
                if (market.getLast().equals(now.getLast())) {
                    //量未变化或量为0
//                    if (market.getVolume().equals(now.getVolume())) {
//                        log.debug("价格和量未变{}", market);
//                        return false;
//                    }
                    return false;
                }
                return true;
            }
            log.debug("延迟行情{}", market);
            //return false;
            return market.getTimestamp().compareTo(now.getTimestamp()) > 0 && !market.getLast().equals(now.getLast());
        } finally {
            lock.unlock();
        }
    }

    private void cacheMarket(Market market, MarketCache marketCache) {
        marketCache.updateNow(market);
        exchangeLastDatatime.put(market.getExchange(), System.currentTimeMillis());
    }


    public Long getLastDataTime(String exchangeName) {
        return exchangeLastDatatime.get(exchangeName);
    }

    public List<MarketCache> getNowMarkets() {
        return new ArrayList<>(exchangeMarkets.values());
    }

    public Market getNow(String onlyKey) {
        MarketCache marketCache = exchangeMarkets.get(onlyKey);
        if (marketCache == null) {
            log.debug("onlyKey获取去重后数据为null，key为{}", onlyKey);
            return null;
        }
        boolean ready = marketCache.isReady();
        if (!ready) {
            log.debug("onlyKey获取去重后数据未准备就绪，key为{}", onlyKey);
        }
        return ready ? marketCache.now() : null;
    }
}
