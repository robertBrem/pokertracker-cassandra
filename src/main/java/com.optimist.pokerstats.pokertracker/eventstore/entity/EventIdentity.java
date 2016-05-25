package com.optimist.pokerstats.pokertracker.eventstore.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventIdentity {
    private final Class<?> entityClass;
    private final Long id;

    public boolean loadAllOfThisType() {
        return id == null;
    }

    @Override
    public String toString() {
        String idSuffix = (loadAllOfThisType()) ? "" : ("-" + id);
        return entityClass.getName() + idSuffix;
    }
}
