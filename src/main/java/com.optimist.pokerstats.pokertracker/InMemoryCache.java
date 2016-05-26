package com.optimist.pokerstats.pokertracker;

import com.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import com.optimist.pokerstats.pokertracker.account.events.AccountPositionCreated;
import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStream;
import com.optimist.pokerstats.pokertracker.player.entity.Player;
import com.optimist.pokerstats.pokertracker.player.events.PlayerCreated;
import com.optimist.pokerstats.pokertracker.player.events.PlayerEvent;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class InMemoryCache {

    @Getter
    private Map<Long, Player> players = new HashMap<>();

    @Getter
    private Map<Long, AccountPosition> accountPositions = new HashMap<>();

    @Inject
    EventStore store;

    @PostConstruct
    public void onInit() {
        EventStream eventStream = store.loadEventStream(Player.class.getName());
        for (CoreEvent event : eventStream.getEvents()) {
            handle(event);
        }
    }

    public void handleEvent(@Observes CoreEvent event) {
        handle(event);
    }

    public void handle(CoreEvent event) {
        if (event instanceof PlayerCreated) {
            ArrayList<CoreEvent> events = new ArrayList<>();
            events.add(event);
            Player player = new Player(events);
            players.put(event.getId(), player);
        } else if (event instanceof PlayerEvent) {
            Player player = players.get(event.getId());
            player.mutate(event);
        } else if (event instanceof AccountPositionCreated) {
            ArrayList<CoreEvent> events = new ArrayList<>();
            events.add(event);
            AccountPosition accountPosition = new AccountPosition(events);
            accountPositions.put(event.getId(), accountPosition);
        }
    }

}
