package com.optimist.pokerstats.pokertracker.account.event;


import lombok.Getter;

@Getter
public class AccountPositionPlayerIdChanged extends AccountPositionEvent {
    private final Long playerId;

    public AccountPositionPlayerIdChanged(Long id, Long playerId) {
        super(id);
        this.playerId = playerId;
    }
}
