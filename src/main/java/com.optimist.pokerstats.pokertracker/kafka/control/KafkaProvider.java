package com.optimist.pokerstats.pokertracker.kafka.control;


import com.google.common.io.Resources;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class KafkaProvider {

    public static final String TOPIC = "test";

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

    public KafkaConsumer<String, String> createConsumer() {
        Properties properties = getProperties("consumer.props");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(TOPIC));
        return consumer;
    }

    public KafkaProducer<String, String> createProducer() {
        Properties properties = getProperties("producer.props");
        return new KafkaProducer<>(properties);
    }

    public Properties getProperties(String file) {
        InputStream props = getFileStream(file);
        Properties properties = new Properties();
        try {
            properties.load(props);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return properties;
    }

    public InputStream getFileStream(String file) {
        try {
            return Resources.getResource(file).openStream();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
