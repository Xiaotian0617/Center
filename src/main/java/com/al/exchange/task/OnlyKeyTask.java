package com.al.exchange.task;

import com.al.exchange.dao.mapper.OnlyKeysConfMapperExt;
import com.al.exchange.util.redis.ObjectRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时获取onlyKey放入redis
 *
 * @author ch.wang
 * @date 2018-05-19 10:32
 */
@Slf4j
@Component
public class OnlyKeyTask {
    @Autowired
    private OnlyKeysConfMapperExt onlyKeysConfMapperExt;

    @Autowired
    private ObjectRedisService redisService;

    @Scheduled(cron = "0 2 * * * ?")
    public void setOnlyKeyToRedis() {
        List<String> onlyKeys = onlyKeysConfMapperExt.selectOnlyKeys();
        for (String onlyKey : onlyKeys) {
            redisService.setHashModel("depth",onlyKey,3000);
        }
    }

}
