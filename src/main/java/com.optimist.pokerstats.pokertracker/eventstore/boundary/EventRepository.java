package com.optimist.pokerstats.pokertracker.eventstore.boundary;

import com.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import com.optimist.pokerstats.pokertracker.account.event.AccountPositionCreated;
import com.optimist.pokerstats.pokertracker.account.event.AccountPositionEvent;
import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStream;
import com.optimist.pokerstats.pokertracker.player.entity.Player;
import com.optimist.pokerstats.pokertracker.player.event.PlayerCreated;
import com.optimist.pokerstats.pokertracker.player.event.PlayerDeleted;
import com.optimist.pokerstats.pokertracker.player.event.PlayerEvent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.util.*;

public class EventRepository {

    @Inject
    EventStore store;

    private Map<Long, AccountPosition> positions = new HashMap<>();
    private Map<Long, Player> players = new HashMap<>();

    public Set<AccountPosition> findAllAccountPositions() {
        positions = new HashMap<>();
        EventStream eventStream = store.loadEventStream(AccountPosition.class.getName());
        for (CoreEvent event : eventStream.getEvents()) {
            handle(event);
        }
        return new HashSet<>(positions.values());
    }

    public Set<Player> findAllPlayers() {
        players = new HashMap<>();
        EventStream eventStream = store.loadEventStream(Player.class.getName());
        for (CoreEvent event : eventStream.getEvents()) {
            handle(event);
        }
        return new HashSet<>(players.values());
    }

    public void handle(CoreEvent event) {
        if (event instanceof PlayerCreated) {
            ArrayList<CoreEvent> events = new ArrayList<>();
            events.add(event);
            Player player = new Player(events);
            players.put(event.getId(), player);
        } else if (event instanceof PlayerDeleted) {
            Player player = players.get(event.getId());
            players.remove(player.getId());
        } else if (event instanceof PlayerEvent) {
            Player player = players.get(event.getId());
            player.mutate(event);
        } else if (event instanceof AccountPositionCreated) {
            ArrayList<CoreEvent> events = new ArrayList<>();
            events.add(event);
            AccountPosition accountPosition = new AccountPosition(events);
            positions.put(event.getId(), accountPosition);
        } else if (event instanceof AccountPositionEvent) {
            AccountPosition accountPosition = positions.get(event.getId());
            accountPosition.mutate(event);
        } else {
            throw new NotImplementedException();
        }
    }

}
