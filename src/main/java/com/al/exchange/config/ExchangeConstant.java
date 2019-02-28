package com.al.exchange.config;

import com.al.exchange.dao.domain.ExchangeBook;
import com.al.exchange.dao.mapper.ExchangeBookMapper;
import com.al.exchange.service.management.OnlyKeyManagement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交易所名称
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 18/01/2018 08:55
 */
@Slf4j
@Component
public class ExchangeConstant {

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    @Autowired
    ExchangeBookMapper exchangeBookMapper;

    private final static ConcurrentHashMap<String, ExchangeBook> exchangeConstantsMap = new ConcurrentHashMap();

    public static String validAndGetExchangeName(String name) {
        ExchangeBook exchangeBook = exchangeConstantsMap.get(name);
        if (exchangeBook == null) {
            return null;
            //throw new RuntimeException("未配置" + name + "对应交易所");
        }
        return exchangeBook.getExchange();
    }

    public ConcurrentHashMap<String, ExchangeBook> getExchangeConstantsMap() {
        return exchangeConstantsMap;
    }

    public ExchangeBook getExchangeConstantsMap(String exchangeName) {
        return exchangeConstantsMap.get(exchangeName);
    }

    public void addExchagne(String exchangeName) {
        ExchangeBook exchangeBook = new ExchangeBook();
        exchangeBook.setExchange(exchangeName);
        exchangeBook.setCname(exchangeName);
        exchangeBook.setSort(99);
        exchangeBook.setUtime(new Date());
        exchangeBookMapper.insertSelective(exchangeBook);
    }

    public static void refresh(List<ExchangeBook> exchangeBooks) {
        for (ExchangeBook exchangeBook : exchangeBooks) {
            exchangeConstantsMap.put(exchangeBook.getExchange(), exchangeBook);
        }
    }
}
