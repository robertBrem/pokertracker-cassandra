package com.optimist.pokerstats.pokertracker.eventstore.control;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class EventStream {
    private final Long version;
    private final List<CoreEvent> events;

    public boolean isEmpty() {
        return events == null || events.isEmpty();
    }
}
