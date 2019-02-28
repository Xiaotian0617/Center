package com.al.exchange.task;


import com.al.exchange.dao.domain.HighLowChange;
import com.al.exchange.dao.domain.MarketCap;
import com.al.exchange.dao.domain.MarketCapExt;
import com.al.exchange.dao.domain.RiseVo;
import com.al.exchange.service.management.AmountChangeManagement;
import com.al.exchange.service.management.HighLowChangeManagement;
import com.al.exchange.service.management.MarketCapManagement;
import com.al.exchange.service.management.PriceChangeManagement;
import com.al.exchange.util.DateUtils;
import com.al.exchange.util.InfluxDbMapper;
import com.al.exchange.util.InfluxResultExt;
import com.al.exchange.util.redis.ObjectRedisService;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时获取市值的涨跌幅，最高（低）价格，成交量.
 *
 * @author ch.wang
 * @version 2018年07月04日14:07:37 V1.0
 * @par 版权信息：
 * 2018 copyright 河南艾鹿网络科技有限公司 all rights reserved.
 */
@Slf4j
@Component
public class MarketValueEnhanceTask {

    @Autowired
    InfluxResultExt influxResultExt;

    @Autowired
    InfluxDbMapper influxDbMapper;

    @Autowired
    MarketCapManagement marketCapManagement;

    @Autowired
    PriceChangeManagement priceChangeManagement;

    @Autowired
    HighLowChangeManagement highLowChangeManagement;

    @Autowired
    AmountChangeManagement amountChangeManagement;

    @Autowired
    ObjectRedisService objectRedisService;

    /**
     * 获取24小时前市值（一分钟执行一次）
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void getLast24HourMarketByTime() {
        log.info("开始维护24小时交易所返回市值信息");
        //Map<String, PriceChange> priceChangeMap = operationFileUtils.getLast24HourMarketFromFile();
        //if (priceChangeMap == null || priceChangeMap.size() == 0) {
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        //TODO 从小时K中获取，暂时不用
        //String queryString = "select * from \"Kline1h\".\"60d\".\"no_vol_kline_1h\"  WHERE time > now() - 24h GROUP BY *";
        long zeroTime = DateUtils.getZeroTime().getTime() * 1000000;
        String queryString = "select last as priceUsd,onlyKey from \"MarketCap\".\"31d\".\"marketCap\"  WHERE time >= now() - 1d GROUP BY * limit 1";
        this.calculationPriceChange(queryString, "marketCap", "24Hour");
    }

    /**
     * 当天0点左右的最近一条数据 (每日凌晨执行).
     */
    @Scheduled(cron = "${scheduled.lastzerotimemarket}")
    void getLastZeroTimeMarketByTime() {
        log.info("开始获取当日凌晨时的市值信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        long zeroTime = DateUtils.getZeroTime().getTime() * 1000000;
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        String queryString = "select last as priceUsd,onlyKey from \"MarketCap\".\"31d\".\"marketCap\" where time = " + zeroTime + " group by *  limit 1";
        this.calculationPriceChange(queryString, "marketCap", "ZeroTime");
    }

    /**
     * 获取距当前时间一小时内的数据（一分钟执行一次）.
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void getLast1HourMarketByTime() {
        log.info("开始获取一小时前的市值信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        //TODO 从小时K中获取，暂时不用
        //String queryString = "select * from \"Kline1h\".\"60d\".\"no_vol_kline_1h\"  WHERE time > now() - 1h GROUP BY *";
        String queryString = "select last as priceUsd,onlyKey from \"MarketCap\".\"31d\".\"marketCap\"  WHERE time >= now() - 1h GROUP BY * limit 1";
        this.calculationPriceChange(queryString, "marketCap", "1Hour");
    }

    /**
     * 获取当日八点的数据（每日八点半执行）.
     */
    @Scheduled(cron = "${scheduled.lasteighttimemarket}")
    void getLastEightTimeMarketByTime() {
        log.debug("开始获取当日8点的市值信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        long eightTime = DateUtils.getEightTime().getTime() * 1000000;
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        String queryString = "select last as priceUsd,onlyKey from \"MarketCap\".\"31d\".\"marketCap\" where time =" + eightTime + " group by *  limit 1";
        this.calculationPriceChange(queryString, "marketCap", "EightTime");
    }

    /**
     * 获取距当前时间7天的数据（一分钟执行一次）.
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void getLast7DayMarketByTime() {
        log.info("开始获取7天前的市值信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        String queryString = "select last,onlyKey from \"MarketCap\".\"31d\".\"marketCap_1D\"  WHERE time >= now() - 7d  GROUP BY * limit 1";
        this.calculationPriceChange(queryString, "marketCap_1D", "7Day");
    }

    /**
     * 获取距当前时间30天的数据（一分钟执行一次）.
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void getLast30DayMarketByTime() {
        log.info("开始获取30天前的市值信息");
        //Map<String, MarketVO> marketVOS = operationFileUtils.getLastZeroTimeMarketFromFile(time);
        //从文件中没有找到 这个条件适用于第一天启动的时候环境中无数据 他会从数据库中查询 如果不想让他查数据库的话 从原环境拷贝这些文件就好
        String queryString = "select last,onlyKey from \"MarketCap\".\"31d\".\"marketCap_1D\"  WHERE time >= now() - 30d GROUP BY * limit 1";
        this.calculationPriceChange(queryString, "marketCap_1D", "30Day");
    }

    private void calculationPriceChange(String queryString, String measurementName, String types) {
        try {
            QueryResult query = influxDbMapper.query(queryString);
            if (null==query){
                log.warn(" 市值{} 类型初始化失败，线上环境涨跌幅可能会受影响！", types);
                return;
            }
            if ("marketCap_1D".equals(measurementName)){
                List<MarketCapExt> klineOthers = influxResultExt.toPOJO(query, MarketCapExt.class, measurementName);
                if (klineOthers == null || klineOthers.size() == 0) {
                    log.warn(" 市值{} 类型初始化失败，线上环境涨跌幅可能会受影响！", types);
                    return;
                }
                for (MarketCapExt klineOther : klineOthers) {
                    if (null==klineOther||!StringUtils.hasText(klineOther.getOnlyKey())){
                        continue;
                    }
                    priceChangeManagement.setPriceByTypeForMarketValue(klineOther.getOnlyKey(), types, klineOther.getLast());
                }
            }else {
                List<MarketCap> klineOthers = influxResultExt.toPOJO(query, MarketCap.class, measurementName);
                if (klineOthers == null || klineOthers.size() == 0) {
                    log.warn(" 市值{} 类型初始化失败，线上环境涨跌幅可能会受影响！", types);
                    return;
                }
                for (MarketCap klineOther : klineOthers) {
                    if (null==klineOther||!StringUtils.hasText(klineOther.getOnlyKey())){
                        continue;
                    }
                    priceChangeManagement.setPriceByTypeForMarketValue(klineOther.getOnlyKey(), types, klineOther.getPriceUsd());
                }
            }
        } catch (Throwable e) {
            log.error("系统查询" + types + "类型市值出现异常，请检查", e);
        }
    }

    /**
     * 获取24小时最高(低)价（）
     */
    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void calculate24HourHighLow() {
        log.info("开始获取最高(低)价");
        try {
            //String queryString = "SELECT * FROM \"MarketCap\".\"31d\".\"marketCap_1D\" where time >= now() - 1d group by * limit 1";
            String queryString = "select max(\"last\") as high,min(\"last\") as low,last(\"onlyKey\") as onlyKey from \"MarketCap\".\"31d\".\"marketCap\"  WHERE time >= now() - 1d GROUP BY *";
            QueryResult query = influxDbMapper.query(queryString);
            if (null==query){
                log.warn("获取市值高低为空！");
                return;
            }
            List<MarketCapExt> klineOthers = influxResultExt.toPOJO(query, MarketCapExt.class, "marketCap");
            if (klineOthers == null || klineOthers.size() == 0) {
                log.warn("获取市值高低为空！");
                return;
            }
            for (MarketCapExt klineOther : klineOthers) {
                if (null==klineOther||!StringUtils.hasText(klineOther.getOnlyKey())){
                    continue;
                }
                HighLowChange highLowChange = new HighLowChange(klineOther.getOnlyKey(), klineOther.getHigh(), klineOther.getLow());
                highLowChangeManagement.putMarketValueHighLowMap(highLowChange.getOnlykey(), highLowChange);
            }
        } catch (Throwable e) {
            log.error("系统获取市值高低出现异常，请检查", e);
        }

    }

    @Scheduled(cron = "${scheduled.last24hourmarket}")
    void calculateamount() {
        log.info("开始计算成交量");
        try {
            calculate24hourvolume();
        } catch (Throwable e) {
            log.error("系统计算24小时成交量出现异常，请检查", e);
        }
    }

    private void calculate24hourvolume() {
        QueryResult queryResult = influxDbMapper.query("select volume,onlyKey from \"MarketCap\".\"31d\".\"marketCap\" where time >= now() - 1h group by * order by time desc limit 1");
        if (queryResult == null) {
            log.warn("获取成交量为空！");
            return;
        }
        List<MarketCapExt> volumeChanges = influxResultExt.toPOJO(queryResult, MarketCapExt.class, "marketCap_1D");
        for (MarketCapExt volumeChange : volumeChanges) {
            if (null==volumeChange||!StringUtils.hasText(volumeChange.getOnlyKey())){
                continue;
            }
            amountChangeManagement.putForMarketValue(volumeChange.getOnlyKey(), volumeChange);
        }
    }

    @Scheduled(cron = "${scheduled.calculatevolume}")
    public void getResiFor7d() {
        String querySqlforCoinName = "SELECT \"last\",onlyKey  FROM \"MarketCap\".\"31d\".\"marketCap_1D\" WHERE time > now() - 8d order by time desc";
        gcomputerResiFor7d(querySqlforCoinName, "marketCap_1D");
        /*String querySqlforOnlyKey = "SELECT  \"close\" as \"last\",onlyKey FROM \"Kline1d\".\"1dINF\".\"kline_1D\" WHERE time > now() - 8d order by time desc";
        gcomputerResiFor7d(querySqlforOnlyKey, "kline_1D");*/
    }

    private void gcomputerResiFor7d(String queryString, String measurementName) {
        List<MarketCapExt> riseVoList = influxResultExt.toPOJO(influxDbMapper.query(queryString), MarketCapExt.class, measurementName);
        List<RiseVo> collect1 = riseVoList.stream().filter(marketCapExt -> StringUtils.hasText(marketCapExt.getOnlyKey())).map(marketCapExt -> new RiseVo(marketCapExt)).collect(Collectors.toList());
        Map<String, List<RiseVo>> collect = collect1.stream().filter((p) -> (p.getLast() != null))
                .collect(Collectors.groupingBy(RiseVo::getOnlyKey));
        for (List<RiseVo> riseVos : collect.values()) {
            marketCapManagement.putMarketCapsFor7dMap(riseVos.get(0).getOnlyKey(), riseVos);
        }
    }


}
