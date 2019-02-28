package com.al.exchange.kafka;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.config.SourceConstant;
import com.al.exchange.dao.domain.*;
import com.al.exchange.service.KlineHandleService;
import com.al.exchange.service.MarketCapHandleService;
import com.al.exchange.service.MarketHandleService;
import com.al.exchange.service.TradeHandleService;
import com.al.exchange.service.management.MarketCapManagement;
import com.al.exchange.service.management.OnlyKeyManagement;
import com.al.exchange.task.PriceRateTask;
import com.al.exchange.util.InfluxDbMapper;
import com.al.exchange.util.redis.ObjectRedisService;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 09/03/2018 15:19
 */
@Component
@Slf4j
public class KafkaReciver {

    @Autowired
    MarketCapManagement marketCapManagement;

    @Autowired
    InfluxDbMapper influxDbMapper;

    @Autowired
    MarketHandleService marketHandleService;

    @Autowired
    TradeHandleService tradeHandleService;

    @Autowired
    MarketCapHandleService marketCapHandleService;

    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    @Autowired
    KlineHandleService klineHandleService;

    @Autowired
    ExchangeConstant exchangeConstant;

    @Resource
    ObjectRedisService objectRedisService;

    @Resource
    PriceRateTask priceRateTask;

    @Value(value = "${real.trade.exchange}")
    private String realTradeExchange;

    private List<MarketCap> tempMarketCap = new ArrayList<>();

    @KafkaListener(topics = "${kafka.topic.market}", groupId = "${kafka.market.groupid}")
    public void receiveMarket(ConsumerRecord consumerRecord) {
        try {
            log.trace("received market topic value='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received market topic value is null !");
                return;
            }
            List<MarketDTO> markets = JSON.parseArray(value.toString(), MarketDTO.class);
            if (markets==null||markets.size()==0||StringUtils.isEmpty(markets.get(0).getExchange())) {
                log.debug("未配置{}对应交易所信息,已跳过", markets);
                return;
            }
            log.trace("数据中心从Kafka中接收的行情信息信息为：{}", markets);
            marketHandleService.marketSaveHandle(markets);
            ArrayList<MarketPO> list = Lists.newArrayList();
            markets.forEach(marketDTO -> {
                MarketPO marketPO = new MarketPO();
                BeanUtils.copyProperties(marketDTO, marketPO);
                marketPO.setExchange(marketDTO.getExchange());
                if (marketDTO.getType() != null) {
                    marketPO.setType(marketDTO.getType().toString());
                }
                if (marketDTO.getFrom() == null) {
                    marketPO.setFrom(SourceConstant.Api.value());
                }
                list.add(marketPO);
            });
            influxDbMapper.writeBeans(list);
        } catch (Throwable e) {
            log.error("接收Kafka中行情信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.marketcap}", groupId = "${kafka.groupid}")
    public void receiveMarketCap(ConsumerRecord consumerRecord) {
        try {
            log.trace("received marketcap topic payload='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received marketcap topic value is null !");
                return;
            }
            List<MarketCapDTO> marketCapDTOS = JSON.parseArray(value.toString(), MarketCapDTO.class);
            List<OnlyKey> onlyKeys = new ArrayList<>();
            marketCapHandleService.marketCapSaveHandle(marketCapDTOS);
            log.debug("数据中心从Kafka中接收的市值信息为：{}", marketCapDTOS);
//            for (MarketCapDTO marketCapDTO : marketCapDTOS) {
//                MarketCapPO marketCapPO = new MarketCapPO();
//                BeanUtils.copyProperties(marketCapDTO, marketCapPO);
//                onlyKeys.add(marketCapPO);
//            }
//            influxDbMapper.writeBeans(onlyKeys);
        } catch (Throwable e) {
            log.error("接收Kafka中市值信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.trade}", groupId = "${kafka.groupid}")
    public void receiveTrade(ConsumerRecord consumerRecord) {
        try {
            log.trace("received marketcap topic payload='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received marketcap topic value is null !");
                return;
            }

            //全部交易列表
            List<TradePO> tradePOS = JSON.parseArray(value.toString(), TradePO.class);
            if(tradePOS.size()>0){
                for(TradePO tradePO:tradePOS){
                    tradePO.setMeasurement("trade");
                }
                tradeHandleService.tradeSaveHandle(tradePOS);
            }
        } catch (Throwable e) {
            log.error("从Kafka中获取交易信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.real-trade}", groupId = "${kafka.groupid}")
    public void receiveRealTrade(ConsumerRecord consumerRecord) {
        try {
            log.trace("received real-trade topic payload='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received real-trade topic value is null !");
                return;
            }

            //全部交易列表
            List<TradePO> tradePOS = JSON.parseArray(value.toString(), TradePO.class);
            if(tradePOS.size()>0){
                tradeHandleService.tradeSaveHandle(tradePOS);
            }
            log.debug("数据中心从Kafka中接收的真实交易信息为：{}", tradePOS);
        } catch (Throwable e) {
            log.error("从Kafka中获取真实交易信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.first-buy-sell}", groupId = "${kafka.groupid}")
    public void receiveFirstBuySell(ConsumerRecord consumerRecord) {
        try {
            log.trace("received firstBuySell topic payload='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received firstBuySell topic value is null !");
                return;
            }

            //全部交易列表
            List<BuyAndSellFirstPO> buyAndSellFirstPOS = JSON.parseArray(value.toString(), BuyAndSellFirstPO.class);
            if(buyAndSellFirstPOS.size()>0){
                influxDbMapper.writeBeans(buyAndSellFirstPOS);
            }
            log.debug("数据中心从Kafka中接收的买一卖一信息为：{}", buyAndSellFirstPOS);
        } catch (Throwable e) {
            log.error("从Kafka中获取买一卖一信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.kline}", id = "exchange-consumer-kline", groupId = "${kafka.groupid}")
    public void receiveKline(ConsumerRecord consumerRecord) {
        try {
            log.trace("received kline topic payload='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received kline topic value is null !");
                return;
            }
            List<KLineDTO> kLineDTOS = JSON.parseArray(value.toString(), KLineDTO.class);
            //2018年6月22日 10:06:26 修改为根据运营设置进行不同的K线保存策略
            klineHandleService.klineSaveHandle(kLineDTOS);
            //influxDbMapper.writeBeans(onlyKeys);
            log.trace("数据中心从Kafka中接收的K线信息为：{}", kLineDTOS);
        } catch (Throwable e) {
            log.error("从数据中心中获取K线数据出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.onlykey_conf}", groupId = "${kafka.groupid}")
    public void receiveOnlyKeyConf(ConsumerRecord consumerRecord) {
        try {
            log.trace("received onlyKeyConf topic payload='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received onlyKeyConf topic value is null !");
                return;
            }
            OnlyKeysMapDto marketCapDTOS = JSON.parseObject(value.toString(), OnlyKeysMapDto.class);
            onlyKeyManagement.resetOnlyKeyconf(marketCapDTOS);
            objectRedisService.setHashModel("keyName", marketCapDTOS.getOnlyKey(), marketCapDTOS.getAllName());
            checkExchangeExist(marketCapDTOS);
            log.debug("数据中心从Kafka中接收的onlyKey配置信息为：{}", marketCapDTOS);
        } catch (Throwable e) {
            log.error("接收Kafka中onlyKey配置信息出错", e);
        }
    }

    private void checkExchangeExist(OnlyKeysMapDto onlyKeysMapDto) {
        ExchangeBook exchangeBook = exchangeConstant.getExchangeConstantsMap(onlyKeysMapDto.getExchange());
        //如果我们库中没有运营设置的这个交易所，数据中心将不入库这些交易所
        // 但如果运维设置了这个交易所的一个OnlyKey，则这个交易所自动加入数据中心允许入库的配置中
        if (exchangeBook == null && StringUtils.hasText(onlyKeysMapDto.getAllName()) && onlyKeysMapDto.getPriceConf() != 0) {
            exchangeConstant.addExchagne(onlyKeysMapDto.getExchange());
            log.info("运维设置了新的交易所{}，数据中心已自动增加", onlyKeysMapDto.getExchange());
        }
    }

    @KafkaListener(topics = "${kafka.topic.info_own}", groupId = "${kafka.groupid}")
    public void receive(ConsumerRecord consumerRecord) {
        try {
            log.trace("received informationOwn topic payload='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received informationOwn topic value is null !");
                return;
            }
            InformationOwnDto informationOwnDto = JSON.parseObject(value.toString(), InformationOwnDto.class);
            onlyKeyManagement.resetInformationOwn(informationOwnDto);
            log.debug("数据中心从Kafka中接收的币种配置信息为：{}", informationOwnDto);
        } catch (Throwable e) {
            log.error("接收Kafka中币种配置信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.rate}", groupId = "${kafka.groupid}")
    public void receiveRate(ConsumerRecord consumerRecord) {
        try {
            log.trace("received informationOwn topic payload='{}' ", consumerRecord);

            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received rate topic value is null !");
                return;
            }
            List<Rate> rates = JSON.parseArray(value.toString(),Rate.class);
            log.debug("数据中心从Kafka中接收的汇率信息为：{}", rates);
            rates.stream().filter(rate -> StringUtils.hasText(rate.getKey())&&rate.getPrice()!=null).forEach(rate -> {
                priceRateTask.getMap().put(rate.getKey(),rate.getPrice());
            });
        } catch (Throwable e) {
            log.error("接收Kafka中汇率信息出错", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.long-short}", groupId = "${kafka.groupid}")
    public void receiveLongShort(ConsumerRecord consumerRecord) {
        try {
            log.trace("received informationOwn topic payload='{}' ", consumerRecord);
            Object value = consumerRecord.value();
            if (value == null) {
                log.warn("received rate topic value is null !");
                return;
            }
            List<LongShortPO> longShortPOS = JSON.parseArray(value.toString(),LongShortPO.class);
            log.debug("数据中心从Kafka中接收的多空信息为：{}", longShortPOS);
            influxDbMapper.writeBeans(longShortPOS);
        } catch (Throwable e) {
            log.error("接收Kafka中汇率信息出错", e);
        }
    }

}
