package com.optimist.pokerstats.pokertracker.player.events;


import lombok.Getter;

@Getter
public class PlayerFirstNameChanged extends PlayerEvent {
    private final String firstName;

    public PlayerFirstNameChanged(Long id, String firstName) {
        super(id);
        this.firstName = firstName;
    }
}
