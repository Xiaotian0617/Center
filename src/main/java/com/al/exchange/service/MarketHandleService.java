package com.al.exchange.service;

import com.al.exchange.config.SourceConstant;
import com.al.exchange.dao.domain.*;
import com.al.exchange.service.management.*;
import com.al.exchange.task.MarketInfoEnhanceTask;
import com.al.exchange.task.PriceRateTask;
import com.al.exchange.util.CalculatePriceUtils;
import com.al.exchange.util.InfluxDbMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * NOTE:
 * 行情数据处理服务
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/22 10:25
 */
@Slf4j
@Service
public class MarketHandleService implements DataHandleService {

    @Autowired
    SourceSettingManagement sourceSettingManagement;

    @Autowired
    InfluxDbMapper influxDbMapper;

    @Autowired
    ExchangeRealtimeMarketService exchangeRealtimeMarketService;

    @Autowired
    WebExchangeRealtimeMarketService webExchangeRealtimeMarketService;

    @Autowired
    MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Autowired
    HighLowChangeManagement highLowChangeManagement;

    @Autowired
    AmountChangeManagement amountChangeManagement;

    @Autowired
    PriceChangeManagement priceChangeManagement;

    @Autowired
    PriceRateTask priceRateTask;

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    private final int ONE = 1;
    private final int TWO = 2;
    private final int THREE = 3;
    private final int FOUR = 4;
    private final int FIVE = 5;


    @Override
    public void saveDatasToDB(List<?> datas) {
        influxDbMapper.writeBeans(datas);
    }

    @Override
    public void saveDataToDB(OnlyKey data) {
        influxDbMapper.writeBean(data);
    }

    /**
     * 市场行情保存处理方法
     *
     * @param markets
     */
    public void marketSaveHandle(List<MarketDTO> markets) {
        List<OnlyKey> onlyKeys = new ArrayList<>();
        for (MarketDTO marketDTO : markets) {
            if (marketDTO == null || !StringUtils.hasText(marketDTO.getOnlyKey())) {
                continue;
            }
            Market market = new Market(marketDTO);
            if (market.getType() == null || market.getType().value.equalsIgnoreCase("api")) {
                market.setType(MarketSourceType.Exchange);
            }
            if (market.getFrom() == null) {
                market.setFrom(SourceConstant.Api);
            }
            //网页抓取也是隶属于交易所数据的一种所以这里就不用type区分了
            if (SourceConstant.Web.equals(market.getFrom())) {
                webExchangeRealtimeMarketService.updateMarket(market);
            } else {
                updateMarketForType(market);
            }
            MarketPO marketPO = new MarketPO();
            BeanUtils.copyProperties(market, marketPO);
            marketPO.setExchange(market.getExchange());
            marketPO.setType(market.getType().toString());
            marketPO.setFrom(market.getFrom().toString());
            SourceSettingManagement.OnlykeySetted onlykeySettedMap = sourceSettingManagement.getMarketSettedMap(marketPO.getOnlyKey());
            if (onlykeySettedMap == null) {
                log.debug("OnlyKey设置未找到，已跳过本Key--{}",market.getOnlyKey());
                continue;
            }
            //先进行实时价格的规则匹配
            if (!lastPriceHandle(marketPO, onlykeySettedMap.getLastPriceSetted())) {
                log.debug("实时价格匹配未找到，已跳过本Key--{}",market.getOnlyKey());
                continue;
            }
            //再进行高低的规则匹配
            highLowHandle(marketPO, onlykeySettedMap.getHighLowSetted());
            //再进行量的规则匹配
            volHandle(marketPO, onlykeySettedMap.getMarketVolSetted());
            String key = marketPO.getOnlyKey();
            PriceChange priceChange = priceChangeManagement.get(key);
            if (priceChange == null) {
                //如果新来的onlyKey之前价格中没有就先加入，默认为0
                priceChangeManagement.initIfAbsent(key);
                priceChange = priceChangeManagement.get(key);
                highLowChangeManagement.initIfAbsent(key);
            }
            marketPO.setChange(accept(marketPO.getLast(), priceChange.getPriceFor24Hour()));
            marketPO.setChangeForEightHour(accept(marketPO.getLast(), priceChange.getPriceFor8Hour()));
            marketPO.setChangeForZeroHour(accept(marketPO.getLast(), priceChange.getPriceFor0Hour()));
            marketPO.setChangeForOneWeek(accept(marketPO.getLast(), priceChange.getPriceFor7Day()));
            marketPO.setChangeForOneHour(accept(marketPO.getLast(), priceChange.getPriceFor1Hour()));
            marketPO.setChangeForOneMouth(accept(marketPO.getLast(), priceChange.getPriceFor1Mouth()));
//            PriceVo bPriceRate = priceRateTask.getPriceRate(marketPO.getUnit()+",USDT");
//            if (bPriceRate.getRate()==null){
//                bPriceRate = priceRateTask.getPriceRate(marketPO.getUnit()+",USD");
//            }
//            PriceVo cPriceRate = priceRateTask.getPriceRate("USD,CNY");
//            marketPO.setBRate(bPriceRate.getRate()==null?new BigDecimal("-1"):bPriceRate.getRate());
//            marketPO.setCRate(cPriceRate.getRate()==null?new BigDecimal("-1"):cPriceRate.getRate());
            onlyKeys.add(marketPO);
        }
        updateFinalMarketsMap(onlyKeys);
        saveDatasToDB(onlyKeys);
    }

    /**
     * 谨慎使用
     * 由于OnlyKey为父类对象 方法中有父类对象强制转换为子类对象
     * 调用此方法，需首先明确自己的onlyKey对象是子类MarketPO的一个示例，否则会抛出异常
     *
     * @param onlyKeys
     */
    private void updateFinalMarketsMap(List<OnlyKey> onlyKeys) {
        Map<String, MarketVO> marketMap = marketInfoEnhanceTask.getMarketMap();
        onlyKeys.forEach(onlyKey -> {
            MarketPO marketPO = (MarketPO) onlyKey;
            String key = marketPO.getOnlyKey();
            OnlyKeysConf onlyKeysConfMap = onlyKeyManagement.getOnlyKeysConfMap(key);
            if (onlyKeysConfMap!=null){
                marketPO.setSymName(onlyKeysConfMap.getAllName());
            }
            marketMap.put(key, new MarketVO(marketPO));
        });
    }

    /**
     * 根据不同的设置完成量的组装
     *
     * @param marketPO
     * @param marketVolSetted
     * @return
     */
    private boolean volHandle(MarketPO marketPO, SourceSettingManagement.MarketVolSetted marketVolSetted) {
        if (marketVolSetted == null) {
            return false;
        }
        int soureSetted = marketVolSetted.getSoureSetted();
        Market webNow = webExchangeRealtimeMarketService.getNow(marketPO.getOnlyKey());
        MarketCapExt volumeChange = amountChangeManagement.get(marketPO.getOnlyKey());
        boolean checkResult = webNow != null && webNow.getVolume() != null;
        switch (soureSetted) {
            case ONE:
//                if (volumeChange==null&&checkResult){
//                    marketPO.setVolume(webNow.getVolume());
//                    break;
//                }
                if (volumeChange != null) {
                    marketPO.setVolume(volumeChange.getVolume());
                } else {
                    marketPO.setVolume(BigDecimal.ZERO);
                }
                break;
            case TWO:
                //优先取网页数据 找不到的话再取交易数据统计
                if (checkResult) {
                    marketPO.setVolume(webNow.getVolume());
                } else {
                    marketPO.setVolume(BigDecimal.ZERO);
                }
                break;
            case THREE:
                //直接使用API
                Market exchNow = exchangeRealtimeMarketService.getNow(marketPO.getOnlyKey());
                if (exchNow != null && exchNow.getVolume() != null) {
                    marketPO.setVolume(exchNow.getVolume());
                }
                break;
            default:
                marketPO.setVolume(BigDecimal.ZERO);
                return false;
        }
        return true;
    }

    /**
     * 根据不同的设置进行高低的组装
     *
     * @param marketPO
     * @param highLowSetted
     * @return
     */
    private boolean highLowHandle(MarketPO marketPO, SourceSettingManagement.MarketHighLowSetted highLowSetted) {
        if (highLowSetted == null) {
            return false;
        }
        int soureSetted = highLowSetted.getSoureSetted();
        Market webNow = webExchangeRealtimeMarketService.getNow(marketPO.getOnlyKey());
        HighLowChange highLowChange = highLowChangeManagement.get(marketPO.getOnlyKey());
        boolean checkResult = (webNow != null && webNow.getHigh() != null && webNow.getLow() != null);
        switch (soureSetted) {
            case ONE:
//                if (checkResult){
//                        marketPO.setHigh(webNow.getHigh());
//                        marketPO.setLow(webNow.getLow());
//                        break;
//                }
                //根据交易计算出量，没有的话取网页数据
                if (highLowChange != null) {
                    marketPO.setHigh(highLowChange.getHigh());
                    marketPO.setLow(highLowChange.getLow());
                } else {
                    marketPO.setHigh(BigDecimal.ZERO);
                    marketPO.setLow(BigDecimal.ZERO);
                }
                break;
            case TWO:
                //优先取网页数据 找不到的话再取交易数据统计
                if (checkResult) {
                    marketPO.setHigh(webNow.getHigh());
                    marketPO.setLow(webNow.getLow());
                } else {
                    marketPO.setHigh(BigDecimal.ZERO);
                    marketPO.setLow(BigDecimal.ZERO);
                }
                //TODO 交易数据统计
//                if (highLowChange!=null){
//                    marketPO.setHigh(highLowChange.getHigh());
//                    marketPO.setLow(highLowChange.getLow());
//                }
                break;
            case THREE:
                //直接使用API
                Market exchNow = exchangeRealtimeMarketService.getNow(marketPO.getOnlyKey());
                if (exchNow != null && exchNow.getHigh() != null && exchNow.getLow() != null) {
                    marketPO.setHigh(exchNow.getHigh());
                    marketPO.setLow(exchNow.getLow());
                }
                break;
            default:
                marketPO.setHigh(BigDecimal.ZERO);
                marketPO.setLow(BigDecimal.ZERO);
                return false;
        }
        return true;
    }

    /**
     * 根据不同的设置进行最新价格的组装
     *
     * @param marketPO
     * @param lastPriceSetted
     * @return
     */
    private boolean lastPriceHandle(MarketPO marketPO, SourceSettingManagement.MarketLastPriceSetted lastPriceSetted) {
        if (lastPriceSetted == null) {
            return false;
        }
        int soureSetted = lastPriceSetted.getSoureSetted();
        Market exchNow = exchangeRealtimeMarketService.getNow(marketPO.getOnlyKey());
        Market webNow = webExchangeRealtimeMarketService.getNow(marketPO.getOnlyKey());
        switch (soureSetted) {
            case ONE:
                //直接使用API
                if (exchNow != null && exchNow.getLast() != null) {
                    marketPO.setLast(exchNow.getLast());
                    marketPO.setFrom(SourceConstant.Api.name());
                } else {
                    marketPO.setLast(BigDecimal.ZERO);
                    marketPO.setFrom(SourceConstant.None.name());
                    return false;
                }
                break;
            case TWO:
                //优先取网页数据 找不到的话再取交易数据统计
                if (webNow != null && webNow.getLast() != null) {
                    marketPO.setLast(webNow.getLast());
                    marketPO.setFrom(SourceConstant.Web.name());
                } else {
                    marketPO.setLast(BigDecimal.ZERO);
                    marketPO.setFrom(SourceConstant.None.name());
                    return false;
                }
                break;
            default:
                marketPO.setLast(BigDecimal.ZERO);
                marketPO.setFrom(SourceConstant.None.name());
                return false;
        }
        return true;
    }

    private void updateMarketForType(Market market) {
        switch (market.getType()) {
            case Exchange:
                exchangeRealtimeMarketService.updateMarket(market);
                break;
            case Other:
                break;
            default:
                break;
        }
    }

    /**
     * 计算其涨跌幅
     *
     * @param newPrice
     * @param oldPrice
     */
    private BigDecimal accept(BigDecimal newPrice, BigDecimal oldPrice) {
        return CalculatePriceUtils.calculatePriceChange(oldPrice, newPrice);
    }


}
