package com.al.exchange;

import com.al.exchange.task.MarketInfoEnhanceTask;
import com.al.exchange.util.redis.ObjectRedisService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MarketInfoTest {

    @Autowired
    MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Autowired
    ObjectRedisService objectRedisService;

    @Test
    public void test() {
        //marketInfoEnhanceTask.calculate24HourHighLow();
        Object calc_rate = objectRedisService.getHashModel("calc_rate", "BTC,USDT");
        System.out.println(calc_rate.toString());
    }
}
