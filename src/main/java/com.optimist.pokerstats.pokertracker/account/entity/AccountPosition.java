package com.optimist.pokerstats.pokertracker.account.entity;

import com.optimist.pokerstats.pokertracker.account.events.AccountPositionCreated;
import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import com.optimist.pokerstats.pokertracker.player.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
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


    @XmlTransient
    private final List<CoreEvent> changes = new ArrayList<>();


    public AccountPosition(List<CoreEvent> events) {
        for (CoreEvent event : events) {
            mutate(event);
        }
    }

    public void create(Long id) {
        apply(new AccountPositionCreated(id));
    }

    public void mutate(CoreEvent event) {
        when(event);
    }

    public void apply(CoreEvent event) {
        changes.add(event);
        mutate(event);
    }

    public void when(CoreEvent event) {
        if (event instanceof AccountPositionCreated) {
            this.id = event.getId();
        }
    }

    public LocalDateTime getRounded(TemporalUnit groupUnit) {
        LocalDateTime asLDT = LocalDateTime.ofInstant(getDate().toInstant(), ZoneId.systemDefault());
        return asLDT.truncatedTo(groupUnit);
    }
}
