package com.optimist.pokerstats.pokertracker.kafka.control;


import com.optimist.pokerstats.pokertracker.EnvironmentVariableGetter;
import org.apache.kafka.clients.producer.KafkaProducer;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class KafkaProvider {
    public static final String TOPIC = "pokertracker";
    public static final String KAFKA_ADDRESS = "KAFKA_ADDRESS";

    @Inject
    EnvironmentVariableGetter envGetter;

    private KafkaProducer<String, String> producer;

    @PostConstruct
    public void init() {
        this.producer = createProducer();
    }

    @Produces
    public KafkaProducer<String, String> getProducer() {
        return producer;
    }

    public String getKafkaAddress() {
        String address = "localhost:9092";
        String kafkaEnv = envGetter.getEnv(KAFKA_ADDRESS);
        if (kafkaEnv != null && !kafkaEnv.isEmpty()) {
            address = kafkaEnv;
        }
        return address;
    }

    public KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", getKafkaAddress());
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(properties);
    }

}
