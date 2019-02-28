package com.al.exchange;

import com.al.exchange.config.InfluxDBProperties;
import com.al.exchange.dao.domain.MarketSourceType;
import com.al.exchange.service.RealtimeMarketService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
@EnableKafka
@MapperScan(basePackages = "com.al.exchange.dao.mapper")
@Slf4j
public class ExchangeApplication {

    @Autowired
    private InfluxDBProperties properties;

    public static void main(String[] args) {
        SpringApplication.run(ExchangeApplication.class, args);
    }

//    @Bean
//    public ThreadPoolExecutor buildThreadPoolExecutor() {
//        return new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(20));
//    }

    @Bean
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(10);
    }

    @Bean
    public OkHttpClient.Builder buildOkHttpClientBuilder(@Value("${spring.application.proxy.enable}")Boolean proxy,@Value("${spring.application.proxy.ip}")String ip,@Value("${spring.application.proxy.port}")int port) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeout(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeout(), TimeUnit.SECONDS);
        if (proxy) {
            builder.proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(ip, port)));
        }
        return builder;
    }

    @Bean
    public OkHttpClient buildOkHttpClient(OkHttpClient.Builder builder) {
        return builder.build();
    }

    @Bean(name = "realtimemarketservicemap")
    public Map<MarketSourceType, RealtimeMarketService> buildRealtimeMarketServiceMap(List<RealtimeMarketService> realtimeMarketServices) {
        HashMap<MarketSourceType, RealtimeMarketService> realtimeMarketServiceMap = Maps.newHashMap();
        for (RealtimeMarketService realtimeMarketService : realtimeMarketServices) {
            realtimeMarketServiceMap.put(realtimeMarketService.type(), realtimeMarketService);
        }
        return realtimeMarketServiceMap;
    }
}

