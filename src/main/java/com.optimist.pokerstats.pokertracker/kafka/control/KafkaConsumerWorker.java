package com.optimist.pokerstats.pokertracker.kafka.control;


import com.airhacks.porcupine.execution.boundary.Dedicated;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.JsonConverter;
import com.optimist.pokerstats.pokertracker.player.entity.Player;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class KafkaConsumerWorker {

    @Dedicated
    @Inject
    ExecutorService kafka;

    @Inject
    JsonConverter converter;

    @Inject
    EventStore store;

    @Inject
    KafkaProducer<String, String> producer;

    @Inject
    KafkaConsumer<String, String> consumer;

    @PostConstruct
    public void init() {
        System.out.println("KafkaConsumerWorker.init");
        CompletableFuture
                .runAsync(this::handleKafkaEvent, kafka);
    }

    public void handleKafkaEvent() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                switch (record.topic()) {
                    case KafkaProvider.TOPIC:
                        System.out.println("TOPIC");
                        String jsonAsString = record.value();
                        InputStream inputStream = new ByteArrayInputStream(jsonAsString.getBytes(Charset.forName("UTF-8")));
                        JsonObject event = null;
                        try {
                            event = Json.createReader(inputStream).readObject();
                        } catch (Exception e) {
                            // catch and forget
                        }
                        if (event == null) {
                            continue;
                        }
                        System.out.println("event = " + event);
                        String topicName = event.getString("topicName");
                        if (topicName == null) {
                            continue;
                        }
                        System.out.println("topicName = " + topicName);
                        String eventsAsJsonString = converter.convertToJson(store.loadEventStream(Player.class.getName()).getEvents()).toString();
                        System.out.println("eventsAsJsonString = " + eventsAsJsonString);
                        producer.send(new ProducerRecord<>(
                                topicName,
                                eventsAsJsonString));

                        break;
                    default:
                        throw new IllegalArgumentException("Illegal message type: ");
                }
            }
        }
    }
}
