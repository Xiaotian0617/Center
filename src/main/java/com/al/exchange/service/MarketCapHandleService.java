package com.al.exchange.service;

import com.al.exchange.dao.domain.*;
import com.al.exchange.dao.mapper.ext.InformationMarketcapExtMapper;
import com.al.exchange.service.management.*;
import com.al.exchange.task.MarketInfoEnhanceTask;
import com.al.exchange.task.PriceRateTask;
import com.al.exchange.util.CalculatePriceUtils;
import com.al.exchange.util.InfluxDbMapper;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/22 15:38
 */
@Slf4j
@Service
public class MarketCapHandleService implements DataHandleService {

    @Autowired
    InfluxDbMapper influxDbMapper;

    @Autowired
    MarketCapManagement marketCapManagement;

    @Autowired
    SourceSettingManagement sourceSettingManagement;

    @Autowired
    MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Autowired
    PriceRateTask priceRateTask;

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    @Autowired
    AmountChangeManagement amountChangeManagement;

    @Autowired
    HighLowChangeManagement highLowChangeManagement;

    @Autowired
    PriceChangeManagement priceChangeManagement;

    @Resource
    InformationMarketcapExtMapper informationMarketcapExtMapper;

    @Override
    public void saveDatasToDB(List<?> datas) {
        influxDbMapper.writeBeans(datas);
    }

    @Override
    public void saveDataToDB(OnlyKey data) {
        influxDbMapper.writeBean(data);
    }

    public void marketCapSaveHandle(List<MarketCapDTO> marketCapDTOS) {
        List<OnlyKey> onlyKeys = new ArrayList<>();
        List<MarketCapPO> marketCapPOS = new ArrayList<>();
        Map<String, MarketCapDTO> collect = marketCapDTOS.stream().peek(marketCapDTO -> marketCapDTO.setOwnSym(false)).collect(Collectors.toMap(MarketCapDTO::getId, Function.identity()));
        HashMap<String, InformationOwnDto> informationOwnDtoHashMap = onlyKeyManagement.getInformationOwnDtoHashMap();
        if (!MapUtils.isEmpty(informationOwnDtoHashMap)) {
            informationOwnDtoHashMap.entrySet().stream().filter(key -> StringUtils.hasText(key.getValue().getSymbol()))
                    .forEach(key -> {
                        String coinMarketCapNameMapByName = onlyKeyManagement.getCoinMarketCapNameMapByName(key.getValue().getName());
                        MarketCapDTO marketCapDTO;
                        MarketCapPO marketCapPO;
                        String ownName = key.getValue().getName();
                        if (ownName == null) {
                            return;
                        }
                        if (!StringUtils.hasText(coinMarketCapNameMapByName)||collect.get(coinMarketCapNameMapByName) == null) {
                            marketCapDTO = new MarketCapDTO(key.getValue());
                            marketCapPO = new MarketCapPO();
                            marketCapDTO.setLastUpdated(System.currentTimeMillis());
                            BeanUtils.copyProperties(marketCapDTO, marketCapPO);
                        }else {
                            marketCapPO = new MarketCapPO();
                            marketCapDTO = collect.get(coinMarketCapNameMapByName);
                            marketCapDTO.setLastUpdated(marketCapDTO.getLastUpdated() * 1000);
                            marketCapDTO.setName(ownName);
                            BeanUtils.copyProperties(marketCapDTO, marketCapPO);
                         }
                        SourceSettingManagement.MarketCapSetted marketCapSettedMap = sourceSettingManagement.getMarketCapSettedMap(ownName);
                        Map<String, MarketVO> marketMap = marketInfoEnhanceTask.getMarketMap();
                        if (marketCapSettedMap != null) {
                            //首先进行价格的权重判断
                            lastPriceHandle(marketCapPO, marketCapSettedMap.getPriceWeights(), marketMap);
                            //其次进行量的相加
                            volHandle(marketCapPO, marketCapSettedMap.getVolSetteds(), marketMap);
                        }
                        marketCapPO.setOnlyKey(ownName);
                        highLowHandle(marketCapPO, null);
                        PriceChange priceChange = priceChangeManagement.getForMarketValue(ownName);
                        if (priceChange == null) {
                            priceChangeManagement.initIfAbsent(ownName);
                            priceChange = priceChangeManagement.get(ownName);
                        }
                        marketCapPO.setPercentChange1h(accept(marketCapPO.getPriceUsd(), priceChange.getPriceFor1Hour()));
                        marketCapPO.setPercentChange7d(accept(marketCapPO.getPriceUsd(), priceChange.getPriceFor7Day()));
                        marketCapPO.setPercentChange24h(accept(marketCapPO.getPriceUsd(), priceChange.getPriceFor24Hour()));
                        marketCapPO.setChangeForEightHour(accept(marketCapPO.getPriceUsd(), priceChange.getPriceFor8Hour()));
                        marketCapPO.setChangeForOneMouth(accept(marketCapPO.getPriceUsd(), priceChange.getPriceFor1Mouth()));
                        marketCapPO.setChangeForZeroHour(accept(marketCapPO.getPriceUsd(), priceChange.getPriceFor0Hour()));
                        marketCapPO.setM("marketCap");
                        if (marketCapPO.getPriceUsd()!=null&&marketCapPO.getPriceUsd().compareTo(BigDecimal.ZERO)>0){
                            marketCapPOS.add(marketCapPO);
                            onlyKeys.add(marketCapPO);
                        }
            });
        }
        informationMarketcapExtMapper.updateOrAddInfomationMarketCaps(marketCapDTOS);
        saveDatasToDB(onlyKeys);
        //将处理过的MarketPO更新进入Map中
        List<MarketCap> marketCaps = JSON.parseArray(JSON.toJSONString(marketCapPOS), MarketCap.class)
                .stream().sorted(Comparator.comparingInt(MarketCap::getRank)).collect(Collectors.toList());
        marketCapManagement.refresh(marketCaps);
    }

    private boolean highLowHandle(MarketCapPO marketCapPO, SourceSettingManagement.MarketCapSetted marketCapSettedMap) {
        HighLowChange marketValue = highLowChangeManagement.getMarketValue(marketCapPO.getOnlyKey());
        if (marketValue == null) {
            highLowChangeManagement.initIfAbsentMarketValueHighLowMap(marketCapPO.getOnlyKey());
        }
        marketCapPO.setHigh(highLowChangeManagement.getMarketValue(marketCapPO.getOnlyKey()).getHigh());
        marketCapPO.setLow(highLowChangeManagement.getMarketValue(marketCapPO.getOnlyKey()).getLow());
        return true;
    }

    /**
     * 量的处理方法
     * 把设置的几个交易对的量加起来
     *
     * @param marketCapPO
     * @param marketCapVolSetteds
     * @param marketMap
     * @return
     */
    private void volHandle(MarketCapPO marketCapPO, List<SourceSettingManagement.MarketCapVolSetted> marketCapVolSetteds, Map<String, MarketVO> marketMap) {
        if (marketCapVolSetteds == null || marketCapVolSetteds.size() == 0) {
            //marketCapPO.setAllDayVolumeUsd(BigDecimal.ZERO);
            return;
        }
        BigDecimal volSum = marketCapVolSetteds.stream().map(marketCapVolSetted -> {
            MarketVO marketVO = marketMap.get(marketCapVolSetted.getOnlyKey());
            if (marketVO == null) {
                return null;
            }
            return marketVO.getVolume();
        }).filter(bigDecimal -> bigDecimal != null).map(o -> o.abs()).reduce(BigDecimal.ZERO, BigDecimal::add);
        marketCapPO.setAllDayVolumeUsd(volSum);
    }

    /**
     * 最新价格的处理逻辑
     * 取这些交易对的最新价，用加权平均值的方式计算（交易所旁边的数字就是权重），没有取到这个价格的，就不采用。
     * ps：“Huobi_BTC_ETH”这个就需要换算成美元，在计算。每5秒计算一次。（如果某个交易所出问题了，怎么判断他出问题出问题后返回什么）
     *
     * @param marketCapPO
     * @param marketCapPriceWeightSetteds
     * @param marketMap
     * @return
     */
    private void lastPriceHandle(MarketCapPO marketCapPO, List<SourceSettingManagement.MarketCapPriceWeightSetted> marketCapPriceWeightSetteds, Map<String, MarketVO> marketMap) {
        if (marketCapPriceWeightSetteds == null || marketCapPriceWeightSetteds.size() == 0) {
            return;
        }
        Map<String, BigDecimal> collect = marketCapPriceWeightSetteds
                .stream()
                .collect(Collectors.toMap(SourceSettingManagement.MarketCapPriceWeightSetted::getOnlyKey, SourceSettingManagement.MarketCapPriceWeightSetted::getWeight));
        BigDecimal btcTotal = BigDecimal.ZERO;
        BigDecimal usdTotal = BigDecimal.ZERO;
        BigDecimal multiplyBTCResult = BigDecimal.ZERO;
        BigDecimal multiplyUSDResult = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> key : collect.entrySet()) {
            MarketVO marketVO = marketMap.get(key.getKey());
            if (marketVO == null) {
                continue;
            }
            //这里有点需要注意 后台设置的价格可能会设置一些非USDT或USD的交易对，这里需要请求汇率换算
            BigDecimal convertedUSDPrice = priceRateConvert(key.getKey()
                    , marketVO.getLast(), "USD");
//            BigDecimal convertedBTCPrice = priceRateConvert(key.getKey()
//                    ,marketVO.getLast(),"BTC");
            if (convertedUSDPrice.compareTo(BigDecimal.ZERO) != 0) {
                usdTotal = usdTotal.add(key.getValue());
                multiplyUSDResult = multiplyUSDResult.add(convertedUSDPrice.multiply(key.getValue()));
            }
//            if (convertedBTCPrice.compareTo(BigDecimal.ZERO)!=0){
//                btcTotal = btcTotal.add(key.getValue());
//                multiplyBTCResult = multiplyBTCResult.add(convertedBTCPrice.multiply(key.getValue()));
//            }
        }
        if (multiplyUSDResult.compareTo(BigDecimal.ZERO) != 0 && usdTotal.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal usdPrice = multiplyUSDResult.divide(usdTotal, 8, BigDecimal.ROUND_HALF_DOWN);
            marketCapPO.setPriceUsd(usdPrice);
        } else {
            marketCapPO.setPriceUsd(BigDecimal.ZERO);
        }
//        if (multiplyBTCResult.compareTo(BigDecimal.ZERO)!=0&&btcTotal.compareTo(BigDecimal.ZERO)!=0){
//            BigDecimal btcPrice = multiplyBTCResult.divide(btcTotal, 8, BigDecimal.ROUND_HALF_DOWN);
//            marketCapPO.setPriceBtc(btcPrice);
//            return;
//        }else {
//            marketCapPO.setPriceBtc(BigDecimal.ZERO);
//        }
    }

    /**
     * 根据用户设置交易对汇率换算其对USD和BTC的汇率
     *
     * @param key  onlyKey
     * @param last 此OnlyKey的最新价格
     * @param type 由于暂时不换取BTC的价格所以这里默认传值为USD
     * @return
     */
    private BigDecimal priceRateConvert(String key, BigDecimal last, String type) {
        String[] split = key.split("_");
        if (split.length != 3) {
            return BigDecimal.ZERO;
        }
//        if ("USDT".equalsIgnoreCase(type)){
//            type = "USD";
//        }
        if (split[2].equalsIgnoreCase(type)) {
            return BigDecimal.ONE.multiply(last).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        }
        PriceVo usdtUsdRate = priceRateTask.getPriceRate("USDT,USD");
        if ("USDT".equalsIgnoreCase(type)) {
            return usdtUsdRate.getRate().multiply(last).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        }
        StringBuilder sb1 = new StringBuilder(split[2]).append(",").append("USD");
        PriceVo priceRate;
        priceRate = priceRateTask.getPriceRate(sb1.toString());
        //获取汇率
        if (priceRate.getRate() != null) {
            return priceRate.getRate().multiply(last).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        }
        StringBuilder sb2 = new StringBuilder(split[2]).append(",").append("USDT");
        priceRate = priceRateTask.getPriceRate(sb2.toString());
        //获取汇率
        if (priceRate.getRate() != null) {
            return priceRate.getRate().multiply(last).multiply(usdtUsdRate.getRate()).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        }
        return BigDecimal.ZERO;
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
