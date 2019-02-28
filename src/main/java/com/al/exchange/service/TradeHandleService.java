package com.al.exchange.service;

import com.al.exchange.dao.domain.OnlyKey;
import com.al.exchange.dao.domain.TradePO;
import com.al.exchange.task.PriceRateTask;
import com.al.exchange.util.InfluxDbMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/22 15:29
 */
@Service
public class TradeHandleService implements DataHandleService {


    @Autowired
    InfluxDbMapper influxDbMapper;

    @Autowired
    PriceRateTask priceRateTask;

    @Override
    public void saveDatasToDB(List<?> datas) {
        influxDbMapper.writeBeans(datas);
    }

    @Override
    public void saveDataToDB(OnlyKey data) {
        influxDbMapper.writeBean(data);
    }


    public void tradeSaveHandle(List<TradePO> tradePOS) {
        List<OnlyKey> onlyKeys = new ArrayList<>();
        for (TradePO tradePO : tradePOS) {
//            PriceVo bPriceRate = priceRateTask.getPriceRate(tradePO.getUnit()+",USDT");
//            if (bPriceRate.getRate()==null){
//                bPriceRate = priceRateTask.getPriceRate(tradePO.getUnit()+",USD");
//            }
//            PriceVo cPriceRate = priceRateTask.getPriceRate("USD,CNY");
//            tradePO.setBRate(bPriceRate.getRate()==null?new BigDecimal("-1"):bPriceRate.getRate());
//            tradePO.setCRate(cPriceRate.getRate()==null?new BigDecimal("-1"):cPriceRate.getRate());
            onlyKeys.add(tradePO);
        }
        saveDatasToDB(onlyKeys);
    }
}
