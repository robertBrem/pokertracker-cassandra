package com.optimist.pokerstats.pokertracker.account.entity;

import com.optimist.pokerstats.pokertracker.LocalDateTimeAdapter;
import com.optimist.pokerstats.pokertracker.account.events.*;
import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import lombok.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(of = {"id"})
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountPosition {
    private Long id;
    private Long playerId;
    private Long amount;
    private String currency;
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime creationDate;


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
        } else if (event instanceof AccountPositionPlayerIdChanged) {
            this.playerId = ((AccountPositionPlayerIdChanged) event).getPlayerId();
        } else if (event instanceof AccountPositionAmountChanged) {
            this.amount = ((AccountPositionAmountChanged) event).getAmount();
        } else if (event instanceof AccountPositionCurrencyChanged) {
            this.currency = ((AccountPositionCurrencyChanged) event).getCurrency();
        } else if (event instanceof AccountPositionCreationDateChanged) {
            this.creationDate = ((AccountPositionCreationDateChanged) event).getCreationDate();
        } else {
            throw new NotImplementedException();
        }
    }

    public void changePlayerId(Long playerId) {
        apply(new AccountPositionPlayerIdChanged(id, playerId));
    }

    public void changeAmount(Long amount) {
        apply(new AccountPositionAmountChanged(id, amount));
    }

    public void changeCurrency(String currency) {
        apply(new AccountPositionCurrencyChanged(id, currency));
    }

    public void changeCreationDate(LocalDateTime creationDate) {
        apply(new AccountPositionCreationDateChanged(id, creationDate));
    }

    public LocalDateTime getRounded(TemporalUnit groupUnit) {
        return getCreationDate().truncatedTo(groupUnit);
    }
}
