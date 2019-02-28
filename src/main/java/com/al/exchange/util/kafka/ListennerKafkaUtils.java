package com.al.exchange.util.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Optional;

/**
 * file:ListennerKafkaUtils
 * <p>
 * 数据中心监听Kafka数据方法
 *
 * @author 11:03  王楷
 * @author yangjunxiao
 * @version 11:03 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
//@Component
public class ListennerKafkaUtils {

    //@KafkaListener(topics= "${kafka.topic.websocket}")
    public void processMessage(String content) {
        // TODO
        log.trace(">>>> received message:{} from topic testTopic", content);
    }


    @KafkaListener(topics = "${kafka.topic.market}", groupId = "123213213", id = "123123213222")
    public void listen(ConsumerRecord<?, ?> record) {
        Optional kafkaMessage = Optional.ofNullable(record.value());
        log.trace(">>>>>>>>>>>>>>>>>>>>");
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            try {
                log.trace("接收到消息，消息为{}", message.toString());
            } catch (Exception e) {
                log.error("异常, 消息:{}, 异常:{}", message.toString(), e);
            }
        }
    }


}
