package com.optimist.pokerstats.pokertracker.account.entity;

import com.optimist.pokerstats.pokertracker.player.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountPosition {
    private Long id;
    private Player player;
    private Long amount;
    private String currency;
    private Date date;
    private Date timestamp;

    public LocalDateTime getRounded(TemporalUnit groupUnit) {
        LocalDateTime asLDT = LocalDateTime.ofInstant(getDate().toInstant(), ZoneId.systemDefault());
        return asLDT.truncatedTo(groupUnit);
    }
}
