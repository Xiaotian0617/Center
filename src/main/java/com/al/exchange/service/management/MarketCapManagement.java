package com.al.exchange.service.management;

import com.al.exchange.dao.domain.MarketCap;
import com.al.exchange.dao.domain.RiseVo;
import com.al.exchange.util.PagingTrans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 市值管理
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 02/03/2018 17:44
 */
@Component
@Slf4j
public class MarketCapManagement {

    @Autowired
    private OnlyKeyManagement onlyKeyManagement;

    //PriceTask 每15秒会更新一次这个Set
    private static final List<MarketCap> marketCaps = new ArrayList<>();

    //Key 为marketCap id
    private static final Map<String, MarketCap> marketCapsMap = new HashMap<>();

    //Key 为marketCap 币种简称
    private static final Map<String, MarketCap> marketCapsMapForSymbol = new HashMap<>();


    //Key 为marketCap 币种全称
    private static final Map<String, MarketCap> marketCapsMapForName = new HashMap<>();

    private static final Map<String, List<RiseVo>> marketCapsFor7dMap = new HashMap<>();

    //由于部分交易所如Okex 会提供一些合约交易 会导致来匹配市值信息时出现匹配不到的情况 所以此List 进行一些常用的标识 以备其他地方调用时替换
    private static final List<String> contractReplace = new ArrayList<String>() {{
        add("THISWEEK");
        add("NEXTWEEK");
        add("QUARTER");
    }};

    ReentrantLock lock = new ReentrantLock();
    private boolean isRefreshed;

    public List<MarketCap> getMarketCaps(int pageNum, int pageSize) {
        if (pageNum == 0 && pageSize == 0) {
            return getMarketCaps();
        }
        synchronized (marketCaps) {
            PagingTrans pagingTrans = new PagingTrans(pageNum, pageSize, marketCaps).invoke();
            int from = pagingTrans.getFrom();
            int to = pagingTrans.getTo();
            return new ArrayList<>(marketCaps.subList(from, to));
        }
    }

    public List<MarketCap> getMarketCaps() {
        synchronized (marketCaps) {
            return new ArrayList<>(marketCaps);
        }
    }

    public static Map<String, MarketCap> getMarketCapsMap() {
        return marketCapsMap;
    }

    public void refresh(List<MarketCap> list) {
        synchronized (marketCaps) {
            marketCaps.clear();
            marketCaps.addAll(list);
            //更新Map信息
            marketCaps.forEach(marketCap -> {
                marketCapsMap.put(marketCap.getId(), marketCap);
                marketCapsMapForSymbol.put(marketCap.getSymbol(), marketCap);
                marketCapsMapForName.put(marketCap.getName(), marketCap);
            });
        }
    }

    public MarketCap getMarketCaps(String id) {
        return getMarketCapsMap().get(id);
    }

    private Map<String, MarketCap> getMarketCapsMapForSymbol() {
        return marketCapsMapForSymbol;
    }

    public MarketCap getMarketCapsBySymbol(String symbol) {
        return getMarketCapsMapForSymbol().get(checkIsContains(symbol));
    }

    public Map<String, MarketCap> getMarketCapsMapForName() {
        return marketCapsMapForName;
    }

    public MarketCap getMarketCapsMapByName(String name) {
        return getMarketCapsMapForName().get(name);
    }

    private String checkIsContains(String symbol) {
        for (int i = 0; i < contractReplace.size(); i++) {
            if (symbol.contains(contractReplace.get(i))) {
                return symbol.replace(symbol, contractReplace.get(i));
            }
        }
        return symbol;
    }

    public List<String> getContractReplace() {
        return contractReplace;
    }

    public static void main(String[] args) {
        System.out.println("BTCTHISWEEK".contains("BTC"));
    }

    public static List<RiseVo> getMarketCapsFor7d(String key) {
        return marketCapsFor7dMap.get(key);
    }

    public void putMarketCapsFor7dMap(String key, List<RiseVo> val) {
        marketCapsFor7dMap.put(key, val);
    }

    public void clearMarketCapsFor7dMap() {
        marketCapsFor7dMap.clear();
        ;
    }
}
