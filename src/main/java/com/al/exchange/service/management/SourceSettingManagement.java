package com.al.exchange.service.management;

import com.al.exchange.dao.domain.OnlyKeysMapDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NOTE: 后台管理设置的来源切换
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/22 9:19
 */
@Component
@Slf4j
public class SourceSettingManagement {

    @Autowired
    OnlyKeyManagement onlyKeyManagement;
    /**
     * 币种设置
     * 实时价格的权重 、 量的和 取自哪儿的设置
     * Key 我们自己的全称 Value设置
     */
    private Map<String, MarketCapSetted> marketCapSettedMap = new HashMap<>();

    /**
     * 交易对设置
     * Key onlyKey Value 设置
     */
    private Map<String, OnlykeySetted> onlykeySettedMap = new HashMap<>();

    public Map<String, OnlykeySetted> getOnlykeySettedMap() {
        return onlykeySettedMap;
    }

    public void setMarketCapSettedMap(String ownName, MarketCapSetted marketCapSetted) {
        this.marketCapSettedMap.put(ownName, marketCapSetted);
    }

    public void setMarketSettedMap(String onlyKey, OnlykeySetted onlykeySetted) {
        this.onlykeySettedMap.put(onlyKey, onlykeySetted);
    }

    public MarketCapSetted getMarketCapSettedMap(String key) {
        return marketCapSettedMap.get(key);
    }

    public OnlykeySetted getMarketSettedMap(String key) {
        return onlykeySettedMap.get(key);
    }

    public Map<String, MarketCapSetted> getMarketCapSettedMap() {
        return marketCapSettedMap;
    }

    @Data
    public class OnlykeySetted {
        private final OnlyKeysMapDto onlyKeysMapDto;

        public OnlykeySetted(OnlyKeysMapDto onlyKeysMapDto) {
            this.onlyKeysMapDto = onlyKeysMapDto;
        }

        private Map<String, MarketSetted> marketSettedMap = new HashMap<>();
        private MarketVolSetted marketVolSetted;
        private MarketHighLowSetted highLowSetted;
        private MarketLastPriceSetted lastPriceSetted;
        private MarketMinuteKlineSetted minuteKlineSetted;
        private MarketDayKlineSetted dayKlineSetted;

        public void setMarketVolSetted(MarketVolSetted marketVolSetted) {
            this.marketVolSetted = marketVolSetted;
            addMarketSetted(marketVolSetted);
        }

        private void addMarketSetted(MarketSetted marketSetted) {
            marketSettedMap.put(marketSetted.getClass().getSimpleName(), marketSetted);
        }

        public void setHighLowSetted(MarketHighLowSetted highLowSetted) {
            this.highLowSetted = highLowSetted;
            addMarketSetted(highLowSetted);
        }

        public void setLastPriceSetted(MarketLastPriceSetted lastPriceSetted) {
            this.lastPriceSetted = lastPriceSetted;
            addMarketSetted(lastPriceSetted);
        }

        public void setMinuteKlineSetted(MarketMinuteKlineSetted minuteKlineSetted) {
            this.minuteKlineSetted = minuteKlineSetted;
            addMarketSetted(minuteKlineSetted);
        }

        public void setDayKlineSetted(MarketDayKlineSetted dayKlineSetted) {
            this.dayKlineSetted = dayKlineSetted;
            addMarketSetted(dayKlineSetted);
        }

        public void changeCheck(Long exchangeLastDataTime, Long webLastDataTime) {
            marketSettedMap.forEach((name, marketSetted) -> {
                if (marketSetted.shouldChange(exchangeLastDataTime, webLastDataTime)) {
                    marketSetted.change();
                }
            });
        }

        public String getExchangeName() {
            return onlyKeysMapDto.getExchange();
        }
    }

    @Data
    public abstract class MarketSetted {
        Long exchangeLastTime;
        long exchangeSwitchTime = 60 * 1000;
        protected int soureSetted;

        public boolean shouldChange(Long curentExchangeTime, Long webLastDataTime) {
            if (exchangeLastTime == null) {
                this.exchangeLastTime = System.currentTimeMillis();
                return false;
            }
            if (curentExchangeTime == null) {
                return isShouldChange(System.currentTimeMillis());
            }
            this.exchangeLastTime = curentExchangeTime;
            return false;
        }


        public abstract void change();

        boolean isShouldChange(Long curentExchangeTime) {
            long sub = curentExchangeTime - exchangeLastTime;
            return sub >= exchangeSwitchTime;
        }

        public int getSoureSetted() {
            return soureSetted;
        }

        public void setSoureSetted(int soureSetted) {
            this.soureSetted = soureSetted;
        }
    }

    /**
     * 量设置
     * NOTE:
     * “1”是根据分钟数据的量统计得出，每分钟计算一次（如果频率高，每5分钟计算一次或者每15分钟计算一次也行），没有的话取网页数据。“2”是取网页数据，没有的话再取分钟数据统计
     */
    @Data
    public class MarketVolSetted extends MarketSetted {

        public MarketVolSetted(int soureSetted) {
            this.soureSetted = soureSetted;
        }

//        @Override
//        public boolean shouldChange(Long curentExchangeTime, Long webLastDataTime) {
//            boolean shouldChange = false;
//            if (exchangeLastTime == null) {
//                this.exchangeLastTime = System.currentTimeMillis();
//                return false;
//            }
//            if (curentExchangeTime == null) {
//                return isShouldChange(System.currentTimeMillis());
//            }
//            this.exchangeLastTime = curentExchangeTime;
//            return shouldChange;
//        }

        @Override
        public void change() {
            log.debug("切换{}状态为{}", this.getClass().getSimpleName(), 2);
            this.soureSetted = 2;
        }
    }


    /**
     * 高低设置
     * NOTE:
     * “1”是根据实时价格计算得出，没有的话取网页数据。“2”是取网页数据，没有的话再用实时价格计算。（每分钟更新一次）
     */
    @Data
    public class MarketHighLowSetted extends MarketSetted {

        public MarketHighLowSetted(int soureSetted) {
            this.soureSetted = soureSetted;
        }

//        @Override
//        public boolean shouldChange(Long curentExchangeTime, Long webLastDataTime) {
//            boolean shouldChange = false;
//            if (exchangeLastTime == null) {
//                this.exchangeLastTime = System.currentTimeMillis();
//                return false;
//            }
//            if (curentExchangeTime == null) {
//                return isShouldChange(System.currentTimeMillis());
//            }
//            this.exchangeLastTime = curentExchangeTime;
//            return shouldChange;
//        }

        @Override
        public void change() {
            log.debug("切换{}状态为{}", this.getClass().getSimpleName(), 2);
            this.soureSetted = 2;
        }
    }

    /**
     * 实时价格设置
     * NOTE：
     * ““1”是交易所的api，该交易所的所有交易对5分钟内都没有数据，则转移到爬虫爬取的网页价格，“2”是爬虫爬取交易所网页价格
     */
    @Data
    public class MarketLastPriceSetted extends MarketSetted {
        public MarketLastPriceSetted(int soureSetted) {
            this.soureSetted = soureSetted;
        }

//        @Override
//        public boolean shouldChange(Long curentExchangeTime, Long webLastDataTime) {
//            boolean shouldChange = false;
//            if (exchangeLastTime == null) {
//                this.exchangeLastTime = System.currentTimeMillis();
//                return false;
//            }
//            if (curentExchangeTime == null) {
//                return isShouldChange(System.currentTimeMillis());
//            }
//            if (this.soureSetted == 1) {
//                shouldChange = isShouldChange(curentExchangeTime);
//            }
//            this.exchangeLastTime = curentExchangeTime;
//            return shouldChange;
//        }

        @Override
        public void change() {
            log.debug("切换{}状态为{}", this.getClass().getSimpleName(), 2);
            this.soureSetted = 2;
        }
    }

    /**
     * 一分钟K线设置
     * NOTE：
     * “1”是先自己用分笔数据生成，再用交易所k线补全（api提供的k线）（2分钟内数据不补）“2”是用交易所k线生成
     * “3”由实时价格生成（开高低收有，量没有）
     */
    @Data
    public class MarketMinuteKlineSetted extends MarketSetted {
        /**
         * 来源设置
         */


        public MarketMinuteKlineSetted(int soureSetted) {
            this.soureSetted = soureSetted;
        }

        @Override
        public boolean shouldChange(Long lastTime, Long webLastDataTime) {
            return false;
        }

        @Override
        public void change() {

        }

    }

    /**
     * 日K线设置
     * NOTE：
     * “1”是先自己用分笔数据生成，再用交易所k线补全（api提供的k线）（1天内数据不补）“2”是用交易所k线生成
     * “3”是由每天0点采集的高低和实时价格生成（上一天的实时价格是今天的开盘价，这样生成的k线没有量）
     */
    @Data
    public class MarketDayKlineSetted extends MarketSetted {

        public MarketDayKlineSetted(int soureSetted) {
            this.soureSetted = soureSetted;
        }

        @Override
        public boolean shouldChange(Long lastTime, Long webLastDataTime) {
            return false;
        }

        @Override
        public void change() {

        }
    }


    @Data
    public class MarketCapSetted {
        List<MarketCapPriceWeightSetted> priceWeights;
        List<MarketCapVolSetted> volSetteds;
    }

    /**
     * 价格权重设置
     * ps:
     * Bitfinex_BTC_USDT，10；Huobi_BTC_ETH，10；Okex_BTC_USDT，10；Bibox_BTC_USDT，2
     * NOTE:
     * 取这些交易对的最新价，用加权平均值的方式计算（交易所旁边的数字就是权重），
     * 没有取到这个价格的，就不采用。ps：“Huobi_BTC_ETH”这个就需要换算成美元，在计算。每5秒计算一次。
     */
    @Data
    public class MarketCapPriceWeightSetted {
        /**
         * 唯一Key
         */
        private String onlyKey;
        /**
         * 设置权重
         */
        private BigDecimal weight;

        public MarketCapPriceWeightSetted(String onlyKey, BigDecimal weight) {
            this.onlyKey = onlyKey;
            this.weight = weight;
        }
    }

    /**
     * 量的设置
     * <p>
     * 把几个交易对的量加起来
     */
    @Data
    public class MarketCapVolSetted {
        /**
         * 唯一Key
         */
        private String onlyKey;

        public MarketCapVolSetted(String onlyKey) {
            this.onlyKey = onlyKey;
        }
    }

}
