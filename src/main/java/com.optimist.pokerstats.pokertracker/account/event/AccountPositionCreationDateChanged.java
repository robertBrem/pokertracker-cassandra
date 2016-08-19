package com.optimist.pokerstats.pokertracker.account.event;


import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountPositionCreationDateChanged extends AccountPositionEvent {
    private final LocalDateTime creationDate;

    public AccountPositionCreationDateChanged(Long id, LocalDateTime creationDate) {
        super(id);
        this.creationDate = creationDate;
    }
}
