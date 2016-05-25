package com.optimist.pokerstats.pokertracker.eventstore.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DataWithVersion {
    private final Long version;
    private final String data;
}
