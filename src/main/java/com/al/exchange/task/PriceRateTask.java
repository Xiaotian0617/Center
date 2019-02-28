package com.al.exchange.task;

import com.al.exchange.dao.domain.MarketCap;
import com.al.exchange.dao.domain.MarketVO;
import com.al.exchange.dao.domain.PriceVo;
import com.al.exchange.service.DataProvideService;
import com.al.exchange.service.management.MarketCapManagement;
import com.al.exchange.util.InfluxDbMapper;
import com.al.exchange.util.api.PriceApi;
import com.al.exchange.util.redis.ObjectRedisService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 获取最新的交易行情.
 * <p>
 * 行情单位要转换为人民币，所以之前要先抓取美元人民币汇率。
 * <p>
 * 行情更新频率为每1分钟一次. 更新主要从 okex.com 网站抓取，抓取失败则从次要的bitfinex站抓取，再抓不到到备用网站抓
 * 都没抓到说明网络可能有异常。不处理
 */
@Slf4j
@Service
public class PriceRateTask {

    @Autowired
    private PriceApi priceApi;

    @Resource
    private DataProvideService dataProvideService;

    @Autowired
    private MarketCapManagement marketCapManagement;

    @Resource
    private MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Resource
    private ObjectRedisService objectRedisService;

    @Resource
    InfluxDbMapper influxDbMapper;

    String APPCODE = "APPCODE c156dd9993ef41c6b40d07a99195de98";

    // private static final String FIXER_RATE_URL = "https://api.fixer.io/latest?base=USD&symbols=USD,CNY";

    @Value("${fixer.appKey}")
    private String appKey;

    private static BigDecimal btcPrice;
    private static BigDecimal ethPrice;
    private static BigDecimal USD_CNY_RATE;

    /**
     * 法币汇率Map
     */
    private static Map<String,BigDecimal> priceMap = new HashMap<String,BigDecimal>(){{
        put("USD,CNY", new BigDecimal("6.9133"));
        put("EUR,CNY", new BigDecimal("7.7199"));
        put("EUR,USD", new BigDecimal("1.16657"));
        put("JPY,CNY", new BigDecimal("0.0597"));
        put("USD,KRW", new BigDecimal("1115.7"));
        put("USD,EUR", new BigDecimal("0.8568"));
        put("USD,JPY", new BigDecimal("110.779"));
        put("USD,GBP", new BigDecimal("0.75773"));
    }};

    /**
     * 汇率恒定Map
     */
    private static Map<String,BigDecimal> OneRateMap = new HashMap<String,BigDecimal>(){{
        put("USD,USD", BigDecimal.ONE);
        put("USD,USDT", BigDecimal.ONE);
        put("USDT,USD", BigDecimal.ONE);
        put("USDT,USDT", BigDecimal.ONE);
        put("QQC,CNY", BigDecimal.ONE);
        put("CNT,CNY", BigDecimal.ONE);
        put("CNY,QQC", BigDecimal.ONE);
        put("USC,USD", BigDecimal.ONE);
        put("USD,USC", BigDecimal.ONE);
    }};

    /**
     * 最终汇率Map
     */
    private static Map<String, BigDecimal> map = new HashMap<String, BigDecimal>() {
        {
            put("CNY,USDT", new BigDecimal("0.1436"));
            put("USDT,CNY", new BigDecimal("6.96"));
            put("KRW,USDT", new BigDecimal("0.000891"));
            put("EUR,USDT", new BigDecimal("1.16657"));
            put("JPY,USDT", new BigDecimal("0.009027"));
            put("USDT,KRW", new BigDecimal("1115.7"));
            put("USDT,EUR", new BigDecimal("0.8568"));
            put("USDT,JPY", new BigDecimal("110.779"));
            put("GBP,USDT", new BigDecimal("1.31951"));
            put("USDT,GBP", new BigDecimal("0.75773"));

            putAll(OneRateMap);
            putAll(priceMap);

            /**
             * 部分默认法币汇率，不参与调用次数（但会更新）
             */
            put("CNY,USD", new BigDecimal("0.1446"));
            put("KRW,USD", new BigDecimal("0.000891"));
            put("KRW,CNY", new BigDecimal("0.005925"));
            put("JPY,USD", new BigDecimal("0.009027"));
            put("GBP,USD", new BigDecimal("1.31951"));
        }
    };



    /**
     * 币币汇率的取值map
     */
    private List<String> marketMap = new ArrayList<String>() {
        {
            add("Okex_OKB_USDT");
            add("Zb_USDT_QC");
            add("Zb_BTC_QC");
            add("Zb_ZB_USDT");
            add("Zb_ZB_BTC");
            add("Bibox_DAI_USDT");
            add("Bibox_BIX_USDT");
            add("Bibox_BIX_BTC");
            add("Bibox_BTC_DAI");
            add("Huobi_DAI_USDT");
            add("Huobi_QTUM_USDT");
            add("Huobi_BTC_USDT");
            add("Huobi_HT_USDT");
            add("Exx_BTC_CNYT");
            add("Coinsbank_BTC_GBP");

            add("Huobi_ETH_BTC");
            add("Okex_OKB_BTC");
            add("Binance_BNB_BTC");
            add("Binance_BNB_USDT");
            add("Binance_TRX_USDT");
            add("Binance_TRX_BTC");

            add("Bitfinex_BTC_EUR");
            add("Bitfinex_BTC_JPY");
            add("Upbit_BTC_KRW");
            add("Bitfinex_BTC_JPY");

        }
    };

    private static String legalTender = "CNY,USD_KRW,USD_EUR,USD_JPY,USD_USD,CNY_KRW,USD_CNY,EUR_CNY,EUR_USD,JPY_CNY,JPY_USD,GBP";

    public Map<String, BigDecimal> getMap() {
        return map;
    }


    public void getNewPriceRateFromRedis() {
        List<String> collect = marketMap.stream().map(str -> {
            String[] split = str.split("_");
            return split[1] + "," + split[2];
        }).collect(Collectors.toList());
        collect.addAll(map.keySet().stream().collect(Collectors.toList()));
        collect.forEach(str -> {
            Object calc_rate = objectRedisService.getHashModel("calc_rate", str);
            if (calc_rate != null) {
                map.put(str, new BigDecimal(calc_rate.toString()));
            }
        });
    }

    /**
     * 获取最新比特币行情价格，单位USD
     *
     * @return 返回价格
     */
    public PriceVo getPriceRate(String key) {
        return new PriceVo(key, map.get(key));
    }


    @Scheduled(fixedRate = 3600 * 1000, initialDelay = 60 * 1000)
    public void priceRate() {
        log.info("开始更新法币汇率");
        try {
            Map<String,BigDecimal> temp = new HashMap<>();
            temp.putAll(priceMap);
            temp.keySet().stream().filter(key -> map.get(key).compareTo(BigDecimal.ONE) != 0).forEach((s) -> {
                String[] strs = s.split(",");
                try {
                    String info = priceApi.getRates(strs[0], strs[1]).execute().body();
                    JSONObject jsonObject = JSONObject.parseObject(info);
                    if (!Objects.equals(jsonObject, null) && Objects.equals(jsonObject.getString("status"), "000000")) {
                        map.put(s, jsonObject.getJSONArray("result").getJSONObject(0).getBigDecimal("result"));
                        map.put(strs[1]+","+strs[0], jsonObject.getJSONArray("result").getJSONObject(1).getBigDecimal("result"));
                    }
                    Thread.sleep(2000);
                } catch (IOException e) {
                    log.error("获取法币汇率失败，失败币种:" + s + "，失败原因：", e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (Throwable e) {
            log.error("系统更新法币汇率出现异常，请检查", e);
        }
        log.info("更新法币汇率结束");
    }

    @Scheduled(fixedRate = 15 * 1000, initialDelay = 60 * 1000)
    public void exchangeRate() {
        log.info("开始更新币种汇率");
        try {
            updateUsdUsdtRate();
            List<MarketCap> list = marketCapManagement.getMarketCaps();
            if (list == null || list.size() == 0) {
                log.warn("币币汇率更新异常，从文件中获取MarketCap信息失败！");
                return;
            }
            //维护 DataProvideService 内的市值list 先清除自身再添加
            //marketCapManagement.refresh(list);
            list.stream().filter(marketCap -> {
                return "BTC_ETH_LTC_ BIX_ZB_DAI_QTUM_OKB_BNB_BIGONE_BCH".contains(marketCap.getSymbol());
            }).forEach(marketCap -> {
                if (Objects.equals(null, marketCap.getOnlyKey())||Objects.equals(null,marketCap)) {
                    return;
                }
                if (!Objects.equals(null, marketCap.getPriceUsd())&&BigDecimal.ZERO.compareTo(marketCap.getPriceUsd())!=0) {
                    map.put(marketCap.getSymbol().toUpperCase() + ",USD", marketCap.getPriceUsd()
                            .setScale(4, BigDecimal.ROUND_HALF_UP));
                }
            });
            //2018年6月12日 15:07:22 新增加部分实时行情汇率 Okex_OKB_USDT,Zb_USDT_QC,Zb_ZB_USDT,Bibox_BIX_USDT,Bibox_DAI_USDT,Huobi_QTUM_USDT
            marketMap.forEach(str -> {
                MarketVO marketVO = marketInfoEnhanceTask.getMarketMap().get(str);
                if (marketVO == null || marketVO.getLast() == null || marketVO.getLast().compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }
                String[] split = str.split("_");
                String mapKey1 = split[1] + "," + split[2];
                map.put(mapKey1, marketVO.getLast());
                String mapKey2 = split[2] + "," + split[1];
                map.put(mapKey2, BigDecimal.ONE.divide(marketVO.getLast(), 8, BigDecimal.ROUND_HALF_DOWN));
            });
        } catch (Throwable e) {
            log.error("系统更新币种汇率出现异常，请检查", e);
        }
        Map<String, Object> redisMap = new HashMap<>();
        redisMap.putAll(map);
        objectRedisService.setHashMap("calc_rate", redisMap);
        //K线接口调整，不再保存汇率入库
        influxDbMapper.writeBeans(map.entrySet().stream().map(PriceVo::new).collect(Collectors.toList()));
        log.info("更新币种汇率结束");
    }

    /**
     * 更新USD和USDT之间的汇率
     */
    private void updateUsdUsdtRate() {
        BigDecimal usdCny = map.get("USD,CNY");
        BigDecimal usdtCny = map.get("USDT,CNY");
        BigDecimal usdRateUsdt = usdtCny.divide(usdCny, 4, BigDecimal.ROUND_HALF_UP);
        map.put("USDT,USD",usdRateUsdt);
        map.put("USD,USDT",BigDecimal.ONE.divide(usdRateUsdt,4,BigDecimal.ROUND_HALF_UP));
        updateUsdUsdtToOhterRate();
    }

    /**
     * 根据USD和USDT之间的汇率更新USDT对其他的汇率
     */
    private void updateUsdUsdtToOhterRate() {
        Map<String,BigDecimal> temp = new HashMap<>();
        temp.putAll(map);
        temp.entrySet().stream().filter(stringBigDecimalEntry -> {
            String[] split = stringBigDecimalEntry.getKey().split(",");
            if (split.length!=2||!"USD".equals(split[1])){
                return false;
            }else if(split[0].equals(split[1])||"USDT".equals(split[0])){
                return false;
            }else {
                return true;
            }
        }).forEach(stringBigDecimalEntry -> {
            String[] split = stringBigDecimalEntry.getKey().split(",");
             map.put(split[0]+",USDT",stringBigDecimalEntry.getValue().multiply(map.get("USD,USDT")).setScale(6,BigDecimal.ROUND_HALF_UP));
            //map.put(stringBigDecimalEntry.getKey(),stringBigDecimalEntry.getValue().multiply(map.get("USD,USDT")).setScale(4,BigDecimal.ROUND_HALF_UP));
        });
    }

}



