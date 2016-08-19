package com.optimist.pokerstats.pokertracker.account.boundary;

import com.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import com.optimist.pokerstats.pokertracker.eventstore.boundary.EventRepository;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStream;
import com.optimist.pokerstats.pokertracker.eventstore.entity.EventIdentity;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

@Stateless
public class AccountService {

    @Inject
    EventStore store;

    @Inject
    EventRepository repository;

    public AccountPosition create() {
        Set<AccountPosition> accountPositions = repository.findAllAccountPositions();
        Long maxId = 0L;
        if (!accountPositions.isEmpty()) {
            maxId = accountPositions.parallelStream()
                    .map(AccountPosition::getId)
                    .max(Long::compareTo)
                    .get();
        }

        Long nextId = 0L;
        if (maxId != null) {
            nextId = maxId + 1;
        }

        AccountPosition accountPosition = new AccountPosition(new ArrayList<>());
        accountPosition.create(nextId);
        store.appendToStream(new EventIdentity(AccountPosition.class, nextId), 0L, accountPosition.getChanges());
        return accountPosition;
    }

    public AccountPosition changePlayerId(Long id, Long playerId) {
        EventStream stream = store.loadEventStream(new EventIdentity(AccountPosition.class, id));
        AccountPosition accountPosition = new AccountPosition(stream.getEvents());
        accountPosition.changePlayerId(playerId);
        store.appendToStream(new EventIdentity(AccountPosition.class, id), stream.getVersion(), accountPosition.getChanges());
        return accountPosition;
    }

    public AccountPosition changeAmount(Long id, Long amount) {
        EventStream stream = store.loadEventStream(new EventIdentity(AccountPosition.class, id));
        AccountPosition accountPosition = new AccountPosition(stream.getEvents());
        accountPosition.changeAmount(amount);
        store.appendToStream(new EventIdentity(AccountPosition.class, id), stream.getVersion(), accountPosition.getChanges());
        return accountPosition;
    }

    public AccountPosition changeCurrency(Long id, String currency) {
        EventStream stream = store.loadEventStream(new EventIdentity(AccountPosition.class, id));
        AccountPosition accountPosition = new AccountPosition(stream.getEvents());
        accountPosition.changeCurrency(currency);
        store.appendToStream(new EventIdentity(AccountPosition.class, id), stream.getVersion(), accountPosition.getChanges());
        return accountPosition;
    }

    public AccountPosition changeCreationDate(Long id, LocalDateTime creationDate) {
        EventStream stream = store.loadEventStream(new EventIdentity(AccountPosition.class, id));
        AccountPosition accountPosition = new AccountPosition(stream.getEvents());
        accountPosition.changeCreationDate(creationDate);
        store.appendToStream(new EventIdentity(AccountPosition.class, id), stream.getVersion(), accountPosition.getChanges());
        return accountPosition;
    }

}
