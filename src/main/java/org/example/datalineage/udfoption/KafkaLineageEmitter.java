package org.example.datalineage.udfoption;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.UUID;

public class KafkaLineageEmitter {
    private static final boolean enabled = false;
    private static final KafkaProducer<String, String> producer;

    static {
        Properties props = new Properties();
        props.put("bootstrap.servers", "your-broker:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        if(enabled)
            producer = new KafkaProducer<>(props);
        else
            producer = null;
    }

    public static void emit(String topic, String message) {
        System.out.printf("Sending message: [ %s ] to topic: [ %s ]\n", message, topic);

        if(enabled)
            producer.send(new ProducerRecord<>(topic, UUID.randomUUID().toString(), message));
    }
}
