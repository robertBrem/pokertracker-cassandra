package com.optimist.pokerstats.pokertracker.account.event;


import lombok.Getter;

@Getter
public class AccountPositionCurrencyChanged extends AccountPositionEvent {
    private final String currency;

    public AccountPositionCurrencyChanged(Long id, String currency) {
        super(id);
        this.currency = currency;
    }
}
