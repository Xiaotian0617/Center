package com.al.exchange.task;

import com.al.exchange.config.ExchangeConstant;
import com.al.exchange.dao.domain.ExchangeBook;
import com.al.exchange.dao.domain.ExchangeBookExample;
import com.al.exchange.dao.domain.MarketKey;
import com.al.exchange.dao.mapper.ExchangeBookMapper;
import com.al.exchange.service.DataProvideService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/3 14:43
 */
@Component
@Slf4j
public class ExchangeBookTask {
    private Date lastDate;
    @Autowired
    ExchangeBookMapper exchangeBookMapper;

    @Autowired
    DataProvideService dataProvideService;

    @Autowired
    ExchangeConstant exchangeConstant;

    private ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    private PeriodicTrigger periodicTrigger = new PeriodicTrigger(10, TimeUnit.SECONDS);


    //    @EventListener(value = ApplicationReadyEvent.class)
//    @Order(2)
    public void loadExchangeBook() {
        log.info("start load exchanges info task");
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.schedule(() -> {
            try {
                log.debug("listening exchange book change");
                ExchangeBookExample exchangeBookExample = new ExchangeBookExample();
                if (lastDate != null) {
                    exchangeBookExample.or().andUtimeGreaterThan(lastDate);
                }
                exchangeBookExample.setOrderByClause("utime desc");
                List<ExchangeBook> exchangeBooks = exchangeBookMapper.selectByExample(exchangeBookExample);
                if (exchangeBooks.size() > 0) {
                    log.info("exchange book has {} changes", exchangeBooks.size());
                    lastDate = exchangeBooks.get(0).getUtime();
                    ExchangeConstant.refresh(exchangeBooks);
                }
                log.info("从数据库新增交易所列表，共{}个", exchangeBooks.size());
                log.info("开始判断运维是否增加交易所");
                //检查OnlyKey中是否有新标记的OnlyKey但库中已经有了的这个交易所的，如果有，将其标记为false
                Collection<MarketKey> allOnlyKeys = dataProvideService.getAllOnlyKeys();
                if (CollectionUtils.isEmpty(allOnlyKeys)) {
                    return;
                }
                allOnlyKeys.stream().forEach(onlyKey -> {
                    String[] split = onlyKey.getOnlyKey().split("_");
                    if (split.length != 3) {
                        return;
                    }
                    ExchangeBook exchangeBook = exchangeConstant.getExchangeConstantsMap(split[0]);
                    if (exchangeBook != null) {
                        List<MarketKey> collect = allOnlyKeys.stream().filter(marketKey -> marketKey!=null&&marketKey.isNew()).filter(marketKey -> {
                            String[] split1 = marketKey.getOnlyKey().split("_");
                            if (split1.length != 3) {
                                return false;
                            }
                            return exchangeBook.getExchange().equals(split1[0]);
                        }).collect(Collectors.toList());
                        collect.forEach(marketKey -> marketKey.setNew(false));
                    }
                });
            }catch (Throwable e){
                log.error("更新交易所出错！",e);
            }
            log.info("判断运维是否增加交易所结束");
        }, periodicTrigger);
    }
}
