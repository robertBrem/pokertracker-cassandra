package com.optimist.pokerstats.pokertracker.account.events;

import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccountPositionEvent implements CoreEvent {
    private final Long id;
}
