package com.al.exchange.service;

import com.al.exchange.dao.domain.KLineDTO;
import com.al.exchange.dao.domain.KLinePO;
import com.al.exchange.dao.domain.OnlyKey;
import com.al.exchange.service.management.SourceSettingManagement;
import com.al.exchange.task.PriceRateTask;
import com.al.exchange.util.InfluxDbMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/22 20:49
 */
@Service
public class KlineHandleService implements DataHandleService {

    @Autowired
    InfluxDbMapper influxDbMapper;

    @Autowired
    SourceSettingManagement sourceSettingManagement;

    @Autowired
    PriceRateTask priceRateTask;

    private final String KLINE_MINUTE = "kline";
    private final String KLINE_DAY = "kline_1d";

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


    public void klineSaveHandle(List<KLineDTO> kLineDTOS) {
        if (kLineDTOS.isEmpty()) {
            return;
        }
        List<OnlyKey> onlyKeys = new ArrayList<>();
        for (KLineDTO kLineDTO : kLineDTOS) {
            KLinePO kLinePO = new KLinePO();
            BeanUtils.copyProperties(kLineDTO, kLinePO);
            String key = kLinePO.getOnlyKey();
            if (StringUtils.isEmpty(key)) {
                continue;
            }
//            PriceVo bPriceRate = priceRateTask.getPriceRate(kLinePO.getUnit()+",USDT");
//            if (bPriceRate.getRate()==null){
//                bPriceRate = priceRateTask.getPriceRate(kLinePO.getUnit()+",USD");
//            }
//            PriceVo cPriceRate = priceRateTask.getPriceRate("USD,CNY");
//            kLinePO.setBRate(bPriceRate.getRate()==null?new BigDecimal("-1"):bPriceRate.getRate());
//            kLinePO.setCRate(cPriceRate.getRate()==null?new BigDecimal("-1"):cPriceRate.getRate());
            onlyKeys.add(kLinePO);
//            SourceSettingManagement.OnlykeySetted onlykeySettedMap = sourceSettingManagement.getMarketSettedMap(key);
//            if (onlykeySettedMap == null) {
//                continue;
//            }
//            if (kLinePO.getMeasurement().equalsIgnoreCase(KLINE_MINUTE)) {
//                minuteKlineHandle(onlyKeys, kLinePO, onlykeySettedMap);
//            }
//            if (kLinePO.getMeasurement().equalsIgnoreCase(KLINE_DAY)) {
//                dayKlineHandle(onlyKeys, kLinePO, onlykeySettedMap);
//            }
        }
        saveDatasToDB(onlyKeys);
    }

    /**
     * 日K线根据设置过滤
     *
     * @param onlyKeys
     * @param kLinePO
     * @param onlykeySettedMap
     */
    private void dayKlineHandle(List<OnlyKey> onlyKeys, KLinePO kLinePO, SourceSettingManagement.OnlykeySetted onlykeySettedMap) {
        SourceSettingManagement.MarketDayKlineSetted dayKlineSetted = onlykeySettedMap.getDayKlineSetted();
        if (dayKlineSetted == null) {
            return;
        }
        int soureSetted = dayKlineSetted.getSoureSetted();
        switch (soureSetted) {
            case ONE:
                //使用交易数据生成 然后API补全(2分钟内不补)
                break;
            case TWO:
                //交易所API
                onlyKeys.add(kLinePO);
                break;
            case THREE:
                //由实时价格生成
                break;
            default:
                break;
        }
    }

    /**
     * 分钟K线根据设置过滤
     *
     * @param onlyKeys
     * @param kLinePO
     * @param onlykeySettedMap
     */
    private void minuteKlineHandle(List<OnlyKey> onlyKeys, KLinePO kLinePO, SourceSettingManagement.OnlykeySetted onlykeySettedMap) {
        SourceSettingManagement.MarketMinuteKlineSetted minuteKlineSetted = onlykeySettedMap.getMinuteKlineSetted();
        if (minuteKlineSetted == null) {
            return;
        }
        int soureSetted = minuteKlineSetted.getSoureSetted();
        switch (soureSetted) {
            case ONE:
                //使用交易数据生成 然后API补全(2分钟内不补)
                break;
            case TWO:
                //交易所API
                onlyKeys.add(kLinePO);
                break;
            case THREE:
                //由实时价格生成
                break;
            default:
                break;
        }
    }


}
