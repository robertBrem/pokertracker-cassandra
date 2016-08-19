package com.optimist.pokerstats.pokertracker.account.event;


import lombok.Getter;

@Getter
public class AccountPositionAmountChanged extends AccountPositionEvent {
    private final Long amount;

    public AccountPositionAmountChanged(Long id, Long amount) {
        super(id);
        this.amount = amount;
    }
}
