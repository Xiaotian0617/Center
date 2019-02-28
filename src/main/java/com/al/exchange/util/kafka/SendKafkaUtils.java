package com.al.exchange.util.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * file:PushKafkaUtils
 * <p>
 * 数据中心往Kafka推送数据方法
 *
 * @author 11:03  王楷
 * @author yangjunxiao
 * @version 11:03 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
public class SendKafkaUtils {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.websocket}")
    private String topic;

    public void sendMarkets(String content) {
        try {
            log.debug("Kafka发送至{}频道", topic);
            kafkaTemplate.send(topic, content);
        } catch (Throwable e) {
            log.error("发送至Kafka时出错" + e.getMessage(), e);
        }
    }
}
