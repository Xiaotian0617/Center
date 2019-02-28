package com.al.exchange.service;

import com.al.exchange.dao.domain.MarketSourceType;
import org.springframework.stereotype.Component;

/**
 * 交易所 实时行情service
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 31/01/2018 15:17
 */
@Component
public class ExchangeRealtimeMarketService extends RealtimeMarketService {

    @Override
    public MarketSourceType type() {
        return MarketSourceType.Exchange;
    }
}
