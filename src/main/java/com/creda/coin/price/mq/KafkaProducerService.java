package com.creda.coin.price.mq;

import cn.hutool.json.JSONUtil;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class KafkaProducerService {
    @Value("${spring.kafka.bootstrap-servers}")
    private String broker;
    KafkaProducer producer =null;
    @PostConstruct
    public  void createTopic() {
        Properties producerProperties = new Properties();
        producerProperties.put("buffer.memory", 104857600);
        producerProperties.put("message.max.bytes", 104857600);
        producerProperties.put("max.request.size", 104857600);
        producerProperties.put("bootstrap.servers", broker);
        producerProperties.put("acks", "all");
        producerProperties.put("retries", 0);
        producerProperties.put("batch.size", 16384);
        producerProperties.put("linger.ms", 1);
        producerProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
         producer = new KafkaProducer<>(producerProperties);

    }


    private static final String TOPIC = "solana_profit_history"; // 主题名称
    @Value("${kafka.send.limit:1000}")
    private int limit;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;



    public void sendMessage(List<TokenProfitHistory> tokenProfitHistory) {
        long startTime = System.currentTimeMillis();

        // 分批处理发送
        for (List<TokenProfitHistory> tokenProfitHistories : ListUtils.partition(tokenProfitHistory, limit)) {
            String message = JSONUtil.toJsonStr(tokenProfitHistories);
            log.info("Sending Kafka message: {}", message);

            // 创建一个回调函数处理发送结果
            producer.send(new ProducerRecord<>(TOPIC, message), (metadata, exception) -> {
                if (exception != null) {
                    // 发送失败，记录错误信息
                    log.error("Error sending message to Kafka: {}", exception.getMessage(), exception);
                } else {
                    // 发送成功，记录成功的信息
                    log.info("Message sent successfully to topic {} partition {} at offset {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        }

        log.info("Send Kafka message time: {}", System.currentTimeMillis() - startTime);
    }

  /*  private static void sendKafkaMessage(String payload,
                                         KafkaProducerService<String, String> producer,
                                         String topic)
    {
        log.info("Sending Kafka message: " + payload);
        producer.send(new ProducerRecord<>(topic, payload));
    }*/
}
