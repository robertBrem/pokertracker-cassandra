package com.optimist.pokerstats.pokertracker.player.events;

import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerEvent implements CoreEvent {
    private final Long id;
}
