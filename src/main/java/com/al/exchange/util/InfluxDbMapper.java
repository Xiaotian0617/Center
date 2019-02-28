package com.al.exchange.util;

import com.al.exchange.config.InfluxDBProperties;
import com.al.exchange.dao.domain.Market;
import com.al.exchange.dao.domain.MarketVO;
import com.al.exchange.dao.domain.OnlyKey;
import com.al.exchange.util.api.TopCoin;
import com.al.exchange.util.kafka.SendKafkaUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.http.HttpStatus;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * file:InfluxDbInitialize
 * <p>
 * InfluxDb的初始化方法
 *
 * @author 11:03  王楷
 * @author yangjunxiao
 * @version 11:03 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Service
public class InfluxDbMapper {

    //ExecutorService executorService = Executors.newCachedThreadPool();

    @Resource
    @Qualifier(value = "WriteDBExecutor")
    ThreadPoolExecutor executorService;


    @Autowired
    TopCoin topCoin;

    private InfluxDB influxDB;

    @Value("${websocket.topcoinws.url}")
    private String apiSocketUrl;
    @Autowired
    private InfluxDBProperties properties;

    @Autowired
    private OkHttpClient.Builder okHttpClientBuilder;


    @PostConstruct
    public InfluxDB getConnection() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory
                    .connect(properties.getUrl() + ":" + properties.getPort(), properties.getUserName(), properties.getPassword(), okHttpClientBuilder);
            log.debug("Using InfluxDB '{}' on '{}'", properties.getDataBase(), properties.getUrl());
            if (properties.isGzip()) {
                log.debug("Enabled gzip compression for HTTP requests");
                influxDB.enableGzip();
            }
        }
        influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
        influxDB.setDatabase(properties.getDataBase());
        return influxDB;
    }


    public void writeBean(final OnlyKey bean) {
        writeBeans(Lists.newArrayList(bean));
    }

    private static Callback empty = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            if (response.code() >= HttpStatus.SC_BAD_REQUEST) {
                log.error("调用 WS 推送服务出错" + response);
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            log.error("调用Websocket发送失败", t);
        }

    };

    public void writeBeans(final List<?> beans) {
        executorService.submit(() -> {
            try {
                List<String> collect = beans.stream().map(PointExt::lineProtocal).collect(Collectors.toList());
                influxDB.write(collect);
            } catch (Exception e) {
                log.error("系统在写入InfluxDB时出错，请检查", e);
            }
        });
    }

    @Autowired
    SendKafkaUtils sendKafkaUtils;

    public void postData(Collection<? extends OnlyKey> markets) {
        String sendJson = JSON.toJSONString(markets);
        sendKafkaUtils.sendMarkets(sendJson);
    }

    public void postData(Market market) {
        ArrayList<Market> objects = Lists.newArrayList(market);
        postData(objects);
    }


    /**
     * 查询时序数据库 返回时间精度为秒
     *
     * @param command 查询语句
     * @return
     */
    public QueryResult query(String command) {
        try {
            long startTime = System.currentTimeMillis();
            QueryResult query = influxDB.query(new Query(command, properties.getDataBase()), TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            log.info("本次执行sql 为 {}，执行时间: {}  ms", command, endTime - startTime);
            return query;
        } catch (Throwable e) {
            log.error("查询Influxdb时出现错误，请检查." + command, e);
            return null;
        }
    }

    public void postData(MarketVO marketVO) {
        ArrayList<MarketVO> objects = Lists.newArrayList(marketVO);
        postData(objects);
    }

    /**
     * 查询时序数据库 返回时间精度为毫秒
     *
     * @param command
     * @return
     */
    public QueryResult queryForMilliseconds(String command) {
        try {
            return influxDB.query(new Query(command, properties.getDataBase()), TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            log.error("查询Influxdb时出现错误，请检查." + command, e);
            return null;
        }
    }

    /**
     * 查询时序数据库 返回时间精度为毫秒
     *
     * @param command
     * @return
     */
    public QueryResult queryForNanoseconds(String command) {
        try {
            return influxDB.query(new Query(command, properties.getDataBase()), TimeUnit.NANOSECONDS);
        } catch (Throwable e) {
            log.error("查询Influxdb时出现错误，请检查." + command, e);
            return null;
        }
    }
}
