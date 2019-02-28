package com.al.exchange.service;

import com.al.exchange.dao.domain.MarketSourceType;
import org.springframework.stereotype.Component;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/22 11:51
 */
@Component
public class WebExchangeRealtimeMarketService extends RealtimeMarketService {


    @Override
    public MarketSourceType type() {
        return MarketSourceType.Web;
    }
}
