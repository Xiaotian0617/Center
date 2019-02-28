package com.al.exchange.controller;

import com.al.exchange.dao.domain.*;
import com.al.exchange.service.RealtimeMarketService;
import com.al.exchange.util.InfluxDbMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 接收计算中心推送行情
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 31/01/2018 10:59
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ReceiveMarketController {
    @Autowired
    @Qualifier("realtimemarketservicemap")
    Map<MarketSourceType, RealtimeMarketService> realtimeMarketServices;


    @Autowired
    InfluxDbMapper influxDbMapper;

    @RequestMapping(value = "/receivemarket", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public void receivemarket(@RequestBody List<MarketDTO> markets) {
        for (MarketDTO marketDTO : markets) {
            Market market = new Market(marketDTO);
            if (market.getType() == null) {
                market.setType(MarketSourceType.Exchange);
            }
            RealtimeMarketService realtimeMarketService = realtimeMarketServices.get(market.getType());
            if (realtimeMarketService == null) {
                log.error("{}realmarketservice 未定义", market.getType());
                return;
            }
            realtimeMarketService.updateMarket(market);
        }
    }

    @RequestMapping(value = "/receivemarketcap", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public void receivemarketcap(@RequestBody List<MarketCapDTO> marketCapDTOS) {
        ArrayList<MarketCapPO> marketCaps = Lists.newArrayList();
        for (MarketCapDTO marketCapDTO : marketCapDTOS) {
            MarketCapPO marketCapPO = new MarketCapPO();
            BeanUtils.copyProperties(marketCapDTO, marketCapPO);
            marketCaps.add(marketCapPO);
        }
        influxDbMapper.writeBeans(marketCaps);
    }

    @RequestMapping(value = "/receivekline", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public void receivekline(@RequestBody List<KLineDTO> kLineDTOS) {
        ArrayList<KLinePO> klinePOs = Lists.newArrayList();
        kLineDTOS.forEach(kLineDTO -> {
            KLinePO kLinePO = new KLinePO();
            BeanUtils.copyProperties(kLineDTO, kLinePO);
            klinePOs.add(kLinePO);
        });
        influxDbMapper.writeBeans(klinePOs);
    }

}
