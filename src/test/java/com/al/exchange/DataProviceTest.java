package com.al.exchange;

import com.al.exchange.dao.domain.MarketKey;
import com.al.exchange.service.DataProvideService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

/**
 * file:topcoin
 * <p>
 * 文件简要说明
 *
 * @author 10:01  王楷
 * @version 10:01 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DataProviceTest {

    @Autowired
    private DataProvideService dataProvideService;

    @Test
    public void getAllEXchange() {
        dataProvideService.getAllEXchange().forEach(exchange -> log.debug(exchange.toString()));
    }

    @Test
    public void getAllMarketCaps() {
        dataProvideService.getAllMarketCaps().forEach(marketCap -> log.info(marketCap.toString()));
    }

    @Test
    public void getAllOnlyKeys() {
        Collection<MarketKey> list = dataProvideService.getAllOnlyKeys();
        list.forEach(str -> {
            log.info(str.toString());
            log.info("目前库中最新的交易对数量：" + list.size());
        });
    }

    @Test
    public void getAllMarketBySymbol() {
//        List<MarketVO> list =  dataProvideService.getAllMarketBySymbol(0,3,"ETH");
//        list.forEach(str -> log.info(str.toString()));
    }

    @Test
    public void getAllMarketByExchange() {
//        List<MarketVO> list =  dataProvideService.getAllMarketByExchange("Okex");
//        list.forEach(str -> log.info(str.toString()));
    }

    @Test
    public void getKlineByOnlyKey() {
//        List<KLine> li = dataProvideService.getKlineByOnlyKey("Bitfinex","ETH","BTC",null);
//        li.forEach(str -> log.info(str.toString()));
    }

    @Test
    public void getKlineByOnlyKeyByLastTime() {
//        List<KLine> li = dataProvideService.getKlineByOnlyKey("Bitfinex","ETH","BTC",new Date().getTime());
//        li.stream().forEach(str -> {
//            log.info(str.toString());
//            System.out.println(str.getTimestamp());
//        });
    }

}
