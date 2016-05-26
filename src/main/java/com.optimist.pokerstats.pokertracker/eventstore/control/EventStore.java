package com.optimist.pokerstats.pokertracker.eventstore.control;

import com.optimist.pokerstats.pokertracker.account.events.AccountPositionCreated;
import com.optimist.pokerstats.pokertracker.eventstore.entity.DataWithVersion;
import com.optimist.pokerstats.pokertracker.eventstore.entity.EventIdentity;
import com.optimist.pokerstats.pokertracker.player.events.PlayerCreated;
import com.optimist.pokerstats.pokertracker.player.events.PlayerFirstNameChanged;
import com.optimist.pokerstats.pokertracker.player.events.PlayerLastNameChanged;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventStore {

    @Inject
    Repository repository;

    @Inject
    Event<CoreEvent> eventChannel;

    public void appendToStream(EventIdentity id, Long originalVersion, List<CoreEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        String name = id.toString();
        JsonArray data = convertToJson(events);
        repository.append(id.getId(), name, data, originalVersion);

        events.stream()
                .forEach(e -> eventChannel.fire(e));
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
                .map(this::convertToEvents)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return new EventStream(maxVersion, events);
    }

    public JsonArray convertToJson(List<CoreEvent> events) {
        JsonArrayBuilder eventArray = Json.createArrayBuilder();
        for (CoreEvent event : events) {
            JsonObjectBuilder jsonEvent = Json.createObjectBuilder()
                    .add("name", event.getClass().getName())
                    .add("id", event.getId());
            if (event instanceof PlayerCreated) {
                // no more to do
            } else if (event instanceof PlayerFirstNameChanged) {
                PlayerFirstNameChanged changedEvent = (PlayerFirstNameChanged) event;
                jsonEvent = jsonEvent
                        .add("firstName", changedEvent.getFirstName());
            } else if (event instanceof PlayerLastNameChanged) {
                PlayerLastNameChanged changedEvent = (PlayerLastNameChanged) event;
                jsonEvent = jsonEvent
                        .add("lastName", changedEvent.getLastName());
            } else if (event instanceof AccountPositionCreated) {
                // no more to do
            } else {
                throw new NotImplementedException();
            }
            eventArray.add(jsonEvent);
        }

        return eventArray.build();
    }

    public List<CoreEvent> convertToEvents(String jsonAsString) {
        ArrayList<CoreEvent> events = new ArrayList<>();
        InputStream inputStream = new ByteArrayInputStream(jsonAsString.getBytes(Charset.forName("UTF-8")));
        JsonArray eventArray = Json.createReader(inputStream).readArray();
        for (int i = 0; i < eventArray.size(); i++) {
            JsonObject eventObj = eventArray.getJsonObject(i);
            String name = eventObj.getString("name");
            Long id = eventObj.getJsonNumber("id").longValue();
            if (PlayerCreated.class.getName().equals(name)) {
                PlayerCreated event = new PlayerCreated(id);
                events.add(event);
            } else if (PlayerFirstNameChanged.class.getName().equals(name)) {
                String firstName = eventObj.getString("firstName");
                PlayerFirstNameChanged event = new PlayerFirstNameChanged(id, firstName);
                events.add(event);
            } else if (PlayerLastNameChanged.class.getName().equals(name)) {
                String lastName = eventObj.getString("lastName");
                PlayerLastNameChanged event = new PlayerLastNameChanged(id, lastName);
                events.add(event);
            } else {
                throw new NotImplementedException();
            }
        }
        return events;
    }
}
