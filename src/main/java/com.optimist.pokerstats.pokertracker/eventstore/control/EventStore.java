package com.optimist.pokerstats.pokertracker.eventstore.control;

import com.optimist.pokerstats.pokertracker.eventstore.entity.DataWithVersion;
import com.optimist.pokerstats.pokertracker.eventstore.entity.EventIdentity;
import com.optimist.pokerstats.pokertracker.kafka.control.KafkaProvider;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;
import javax.json.JsonArray;
import java.util.List;
import java.util.stream.Collectors;

public class EventStore {

    @Inject
    Repository repository;

    @Inject
    KafkaProducer<String, String> producer;

    @Inject
    JsonConverter converter;

    public void appendToStream(EventIdentity id, Long originalVersion, List<CoreEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        String name = id.toString();
        JsonArray data = converter.convertToJson(events);
        repository.append(id.getId(), name, data, originalVersion);

        producer.send(new ProducerRecord<>(
                KafkaProvider.TOPIC,
                converter.convertToJson(events).toString()));
    }

    public EventStream loadEventStream(String name) {
        List<DataWithVersion> records = repository.readRecords(name);
        return toEventStream(records);
    }

    public EventStream loadEventStream(EventIdentity id) {
        String name = id.toString();
        List<DataWithVersion> records = repository.readRecords(id.getId(), name);

        return toEventStream(records);
    }

    public EventStream loadEventStream(EventIdentity id, Long minVersion, Integer take) {
        String name = id.toString();
        List<DataWithVersion> records = repository.readRecords(id.getId(), name, minVersion, take);
        return toEventStream(records);
    }

    private EventStream toEventStream(List<DataWithVersion> records) {
        Long maxVersion = 0L;
        if (!records.isEmpty()) {
            maxVersion = records.stream()
                    .map(DataWithVersion::getVersion)
                    .max(Long::compareTo)
                    .get();
        }

        List<CoreEvent> events = records.stream()
                .map(DataWithVersion::getData)
                .map(converter::convertToEvents)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return new EventStream(maxVersion, events);
    }

}
