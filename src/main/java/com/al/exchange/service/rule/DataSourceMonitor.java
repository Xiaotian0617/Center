package com.al.exchange.service.rule;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.service.ExchangeRealtimeMarketService;
import com.al.exchange.service.WebExchangeRealtimeMarketService;
import com.al.exchange.service.management.SourceSettingManagement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 数据源监控
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/23 11:34
 */
@Component
@Slf4j
public class DataSourceMonitor implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    ExchangeRealtimeMarketService exchangeRealtimeMarketService;

    @Autowired
    WebExchangeRealtimeMarketService webExchangeRealtimeMarketService;

    @Autowired
    SourceSettingManagement sourceSettingManagement;
    private ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    private PeriodicTrigger periodicTrigger = new PeriodicTrigger(30, TimeUnit.SECONDS);

    private boolean start = false;

    public void startMonitor() {
        if (!start) {
            log.info("启动数据源监控定时任务");
            periodicTrigger.setInitialDelay(10);
            threadPoolTaskScheduler.setPoolSize(2);
            threadPoolTaskScheduler.setThreadNamePrefix("DataSourceMonitor");
            threadPoolTaskScheduler.initialize();
            threadPoolTaskScheduler.schedule(() -> {
                Map<String, SourceSettingManagement.OnlykeySetted> onlykeySettedMap = sourceSettingManagement.getOnlykeySettedMap();
                onlykeySettedMap.forEach((onlykey, onlykeySetted) -> {
                    String settedName = onlykeySetted.getExchangeName();
                    String exchangeName = ExchangeConstant.validAndGetExchangeName(settedName);
                    if (!StringUtils.hasText(exchangeName)||!StringUtils.hasText(exchangeName)){
                        return;
                    }
                    onlykeySetted.changeCheck(exchangeRealtimeMarketService.getLastDataTime(exchangeName), webExchangeRealtimeMarketService.getLastDataTime(exchangeName));
                });
            }, periodicTrigger);
        }
        this.start = true;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        startMonitor();
    }
}
