package com.optimist.pokerstats.pokertracker.eventstore.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventIdentity {
    private final Class<?> entityClass;
    private final Long id;

    @Override
    public String toString() {
        return entityClass.getName();
    }
}
