package com.al.exchange.task;

import com.al.exchange.dao.domain.MarketCap;
import com.al.exchange.dao.domain.MarketCapVO;
import com.al.exchange.dao.domain.MarketKey;
import com.al.exchange.dao.domain.MarketVO;
import com.al.exchange.service.management.MarketCapManagement;
import com.al.exchange.service.management.OnlyKeyManagement;
import com.al.exchange.service.management.PriceChangeManagement;
import com.al.exchange.util.InfluxDbMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * topcoin
 * file:topcoin
 * <p>
 *
 * @author mr.wang
 * @version 02 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
public class WebSocketPushTask {
    @Value("${websocket.push.market:true}")
    boolean marketPush;

    @Value("${websocket.push.marketcap:true}")
    boolean marketcapPush;

    @Value("${websocket.push.kline:true}")
    boolean klinePush;

    @Autowired
    InfluxDbMapper influxDbMapper;

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    @Resource
    @Qualifier(value = "WebsocketExecutor")
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    PriceChangeManagement priceChangeManagement;

    @Autowired
    MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Autowired
    MarketCapManagement marketCapManagement;

    @Scheduled(fixedRate = 500, initialDelay = 60 * 1000)
    void pushMarketInfo() {
        if (!marketPush) {
            log.debug("行情推送被禁用");
            return;
        }
        Map<String, MarketVO> sendMap = new HashMap<>();
        List<MarketKey> marketKeys = new ArrayList<>(onlyKeyManagement.onlyKeys());
        marketKeys
                .stream()
                .filter(marketKey -> marketKey!=null)
                .filter(marketKey -> {
                    MarketVO marketVO = marketInfoEnhanceTask.getMarketMap().get(marketKey.onlyKey());
                    return marketVO != null
                            && marketVO.getNeedSend() != null && marketVO.getNeedSend();
                })
                .forEach(marketKey -> {
                    try {
                        MarketVO marketVO = marketInfoEnhanceTask.getMarketMap().get(marketKey.onlyKey());
                        sendMap.put(marketKey.onlyKey(), marketVO);
                        marketVO.setNeedSend(false);
                    } catch (Throwable e) {
                        log.error("系统在行情发送时，修改其发送状态出错，请检查" + e.getMessage(), e);
                    }
                });

        log.debug("行情本次推送条数为：{}", sendMap.size());
        if (sendMap.size() == 0) {
            return;
        }
        if (log.isTraceEnabled()) {
            sendMap.forEach((key, value) -> {
                log.trace("推送行情 {}", value);
            });
        }
        threadPoolExecutor.submit(() -> influxDbMapper.postData(sendMap.values()));

    }

    @Scheduled(fixedRate = 15 * 1000, initialDelay = 50 * 1000)
    void pushMarketCapInfo() {
        if (!marketcapPush) {
            log.debug("市值推送被禁用");
            return;
        }
        if (marketCapManagement.getMarketCaps() == null || marketCapManagement.getMarketCaps().size() == 0) {
            return;
        }
        List<MarketCap> list = marketCapManagement.getMarketCaps();
        List<MarketCapVO> collect = list.stream().map(marketCap -> {
            MarketCapVO marketCapVO = new MarketCapVO();
            BeanUtils.copyProperties(marketCap, marketCapVO);
            return marketCapVO;
        }).collect(Collectors.toList());
        log.debug("市值本次推送条数为：{}", collect.size());
        influxDbMapper.postData(collect);
    }
}
