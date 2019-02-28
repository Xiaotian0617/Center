package com.al.exchange.service.management;

import com.al.exchange.dao.domain.MarketCapExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 09/02/2018 12:16
 */
@Component
public class AmountChangeManagement {
    private final Map<String, MarketCapExt> amountChangeMap = new HashMap<>();

    /**
     * 市值中的成交量Map
     */
    private final Map<String, MarketCapExt> marketValueAmountChangeMap = new HashMap<>();

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    public MarketCapExt get(String onlyKey) {
        return amountChangeMap.get(onlyKey);
    }

    public Map<String, MarketCapExt> getAmountChangeMap() {
        return Collections.unmodifiableMap(amountChangeMap);
    }

    public void put(String onlykey, MarketCapExt volumeChange) {
        amountChangeMap.put(onlykey, volumeChange);
    }


    public MarketCapExt getForMarketValue(String onlyKey) {
        return marketValueAmountChangeMap.get(onlyKey);
    }

    public Map<String, MarketCapExt> getAmountChangeMapForMarketValue() {
        return Collections.unmodifiableMap(marketValueAmountChangeMap);
    }

    public void putForMarketValue(String onlykey, MarketCapExt volumeChange) {
        marketValueAmountChangeMap.put(onlykey, volumeChange);
    }
}
