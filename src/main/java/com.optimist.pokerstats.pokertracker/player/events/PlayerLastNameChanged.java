package com.optimist.pokerstats.pokertracker.player.events;

import lombok.Getter;

@Getter
public class PlayerLastNameChanged extends PlayerEvent {
    private final String lastName;

    public PlayerLastNameChanged(Long id, String lastName) {
        super(id);
        this.lastName = lastName;
    }
}
