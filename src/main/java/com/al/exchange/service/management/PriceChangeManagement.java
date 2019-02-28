package com.al.exchange.service.management;

import com.al.exchange.dao.domain.MarketKey;
import com.al.exchange.dao.domain.PriceChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 09/02/2018 12:16
 */
@Component
public class PriceChangeManagement {
    private final static Map<String, PriceChange> priceChangeMap = new HashMap<>();

    private final static Map<String, PriceChange> marketValuePriceChangeMap = new HashMap<>();

    @Autowired
    OnlyKeyManagement onlyKeyManagement;
    private boolean init;

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public Map<String, PriceChange> getAllDataForMarketValue() {
        return marketValuePriceChangeMap;
    }

    public synchronized void init() {
        List<MarketKey> marketKeys = onlyKeyManagement.onlyKeys();
        marketKeys.stream().filter(marketKey -> marketKey!=null&&StringUtils.hasText(marketKey.getOnlyKey()))
                .forEach(marketKey -> priceChangeMap.put(marketKey.onlyKey(), new PriceChange(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));if (priceChangeMap.size() > 0) {
            setInit(true);
        }
    }

    public PriceChange initIfAbsent(String onlyKey) {
        return priceChangeMap.putIfAbsent(onlyKey, new PriceChange(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public PriceChange get(String onlyKey) {
        return priceChangeMap.get(onlyKey);
    }

    public Map<String, PriceChange> getPriceChangeMap() {
        return Collections.unmodifiableMap(priceChangeMap);
    }

    public void setOnlyKeyManagement(String onlyKey, PriceChange priceChange) {
        priceChangeMap.put(onlyKey, priceChange);
    }

    public void setPriceByType(String onlyKey, String types, BigDecimal price) {
        initIfAbsent(onlyKey);
        if (types.equals("30Day")) {
            priceChangeMap.get(onlyKey).setPriceFor1Mouth(price);
        }
        if (types.equals("7Day")) {
            priceChangeMap.get(onlyKey).setPriceFor7Day(price);
        }
        if (types.equals("EightTime")) {
            priceChangeMap.get(onlyKey).setPriceFor8Hour(price);
        }
        if (types.equals("1Hour")) {
            priceChangeMap.get(onlyKey).setPriceFor1Hour(price);
        }
        if (types.equals("ZeroTime")) {
            priceChangeMap.get(onlyKey).setPriceFor0Hour(price);
        }
        if (types.equals("24Hour")) {
            priceChangeMap.get(onlyKey).setPriceFor24Hour(price);
        }
    }


    public synchronized void initMarketValue() {
        List<MarketKey> marketKeys = onlyKeyManagement.onlyKeys();
        marketKeys.forEach(marketKey -> marketValuePriceChangeMap.put(marketKey.onlyKey(), new PriceChange(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));
        if (marketValuePriceChangeMap.size() > 0) {
            setInit(true);
        }
    }

    public PriceChange initIfAbsentForMarketValue(String onlyKey) {
        return marketValuePriceChangeMap.putIfAbsent(onlyKey, new PriceChange(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public PriceChange getForMarketValue(String onlyKey) {
        return marketValuePriceChangeMap.get(onlyKey);
    }

    public Map<String, PriceChange> getMarketValuePriceChangeMap() {
        return Collections.unmodifiableMap(marketValuePriceChangeMap);
    }

    public void setOnlyKeyManagementForMarKetValue(String onlyKey, PriceChange priceChange) {
        marketValuePriceChangeMap.put(onlyKey, priceChange);
    }

    public void setPriceByTypeForMarketValue(String onlyKey, String types, BigDecimal price) {
        initIfAbsentForMarketValue(onlyKey);
        if (types.equals("30Day")) {
            marketValuePriceChangeMap.get(onlyKey).setPriceFor1Mouth(price);
        }
        if (types.equals("7Day")) {
            marketValuePriceChangeMap.get(onlyKey).setPriceFor7Day(price);
        }
        if (types.equals("EightTime")) {
            marketValuePriceChangeMap.get(onlyKey).setPriceFor8Hour(price);
        }
        if (types.equals("1Hour")) {
            marketValuePriceChangeMap.get(onlyKey).setPriceFor1Hour(price);
        }
        if (types.equals("ZeroTime")) {
            marketValuePriceChangeMap.get(onlyKey).setPriceFor0Hour(price);
        }
        if (types.equals("24Hour")) {
            marketValuePriceChangeMap.get(onlyKey).setPriceFor24Hour(price);
        }
    }
}
