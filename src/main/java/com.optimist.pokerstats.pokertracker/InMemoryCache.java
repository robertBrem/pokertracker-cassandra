package com.optimist.pokerstats.pokertracker;

import com.airhacks.porcupine.execution.boundary.Dedicated;
import com.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import com.optimist.pokerstats.pokertracker.account.events.AccountPositionCreated;
import com.optimist.pokerstats.pokertracker.account.events.AccountPositionEvent;
import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStream;
import com.optimist.pokerstats.pokertracker.eventstore.control.JsonConverter;
import com.optimist.pokerstats.pokertracker.kafka.control.KafkaProvider;
import com.optimist.pokerstats.pokertracker.player.entity.Player;
import com.optimist.pokerstats.pokertracker.player.events.PlayerCreated;
import com.optimist.pokerstats.pokertracker.player.events.PlayerDeleted;
import com.optimist.pokerstats.pokertracker.player.events.PlayerEvent;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Singleton
public class InMemoryCache {

    @Getter
    private Map<Long, Player> players = new HashMap<>();

    @Getter
    private Map<Long, AccountPosition> accountPositions = new HashMap<>();

    @Inject
    EventStore store;

    @Inject
    KafkaConsumer<String, String> consumer;

    @Dedicated
    @Inject
    ExecutorService kafka;

    @Inject
    JsonConverter converter;

    @PostConstruct
    public void onInit() {
        EventStream eventStream = store.loadEventStream(Player.class.getName());
        for (CoreEvent event : eventStream.getEvents()) {
            handle(event);
        }
        eventStream = store.loadEventStream(AccountPosition.class.getName());
        for (CoreEvent event : eventStream.getEvents()) {
            handle(event);
        }

        CompletableFuture
                .runAsync(this::handleKafkaEvent, kafka);
    }

    public void handleKafkaEvent() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(20);
            for (ConsumerRecord<String, String> record : records) {
                switch (record.topic()) {
                    case KafkaProvider.TOPIC:
                        System.out.println("record.value() = " + record.value());
                        List<CoreEvent> events = converter.convertToEvents(record.value());
                        for (CoreEvent event : events) {
                            handle(event);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal message type: ");
                }
            }
        }
    }

    public void handle(CoreEvent event) {
        if (event instanceof PlayerCreated) {
            ArrayList<CoreEvent> events = new ArrayList<>();
            events.add(event);
            Player player = new Player(events);
            players.put(event.getId(), player);
        } else if (event instanceof PlayerDeleted) {
            Player player = players.get(event.getId());
            players.remove(player.getId());
        } else if (event instanceof PlayerEvent) {
            Player player = players.get(event.getId());
            player.mutate(event);
        } else if (event instanceof AccountPositionCreated) {
            ArrayList<CoreEvent> events = new ArrayList<>();
            events.add(event);
            AccountPosition accountPosition = new AccountPosition(events);
            accountPositions.put(event.getId(), accountPosition);
        } else if (event instanceof AccountPositionEvent) {
            AccountPosition accountPosition = accountPositions.get(event.getId());
            accountPosition.mutate(event);
        } else {
            throw new NotImplementedException();
        }
    }

}
