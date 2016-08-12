package com.optimist.pokerstats.pokertracker.kafka.control;


import com.optimist.pokerstats.pokertracker.EnvironmentVariableGetter;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Properties;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class KafkaProvider {
    public static final String TOPIC = "pokertracker";
    public static final String KAFKA_ADDRESS = "KAFKA_ADDRESS";

    @Inject
    EnvironmentVariableGetter envGetter;

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

    @PostConstruct
    public void init() {
        this.producer = createProducer();
        this.consumer = createConsumer();
    }

    @Produces
    public KafkaProducer<String, String> getProducer() {
        return producer;
    }

    @Produces
    public KafkaConsumer<String, String> getConsumer() {
        return consumer;
    }

    public String getKafkaAddress() {
        String address = "localhost:9092";
        String kafkaEnv = envGetter.getEnv(KAFKA_ADDRESS);
        if (kafkaEnv != null && !kafkaEnv.isEmpty()) {
            address = kafkaEnv;
        }
        return address;
    }

    public KafkaConsumer<String, String> createConsumer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", getKafkaAddress());
        properties.put("group.id", "pokertracker");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(TOPIC));
        return consumer;
    }

    public KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", getKafkaAddress());
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(properties);
    }

}
