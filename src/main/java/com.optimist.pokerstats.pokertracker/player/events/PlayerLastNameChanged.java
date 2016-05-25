package com.optimist.pokerstats.pokertracker.player.events;

import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerLastNameChanged implements CoreEvent {
    private final Long id;
    private final String lastName;
}
