package com.al.exchange.service.management;

import com.al.exchange.dao.domain.HighLowChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
public class HighLowChangeManagement {
    private final Map<String, HighLowChange> highLowMap = new HashMap<>();

    /**
     * 存放市值高低
     * Key onlyKey
     * value 市值高低
     */
    private final Map<String, HighLowChange> marketValueHighLowMap = new HashMap<>();

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    public HighLowChange get(String onlyKey) {
        return highLowMap.get(onlyKey);
    }

    public HighLowChange initIfAbsent(String onlyKey) {
        return highLowMap.putIfAbsent(onlyKey, new HighLowChange(onlyKey, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public Map<String, HighLowChange> getHighLowMap() {
        return Collections.unmodifiableMap(highLowMap);
    }

    public void put(String onlykey, HighLowChange highLowChange) {
        highLowMap.put(onlykey, highLowChange);
    }

    public HighLowChange getMarketValue(String onlyKey) {
        return marketValueHighLowMap.get(onlyKey);
    }

    public HighLowChange initIfAbsentMarketValueHighLowMap(String onlyKey) {
        return marketValueHighLowMap.putIfAbsent(onlyKey, new HighLowChange(onlyKey, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public Map<String, HighLowChange> getMarketValueHighLowMap() {
        return Collections.unmodifiableMap(marketValueHighLowMap);
    }

    public void putMarketValueHighLowMap(String onlykey, HighLowChange highLowChange) {
        marketValueHighLowMap.put(onlykey, highLowChange);
    }
}
