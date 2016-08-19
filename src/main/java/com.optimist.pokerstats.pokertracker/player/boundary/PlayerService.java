package com.optimist.pokerstats.pokertracker.player.boundary;

import com.optimist.pokerstats.pokertracker.eventstore.boundary.EventRepository;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStream;
import com.optimist.pokerstats.pokertracker.eventstore.entity.EventIdentity;
import com.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Set;

@Stateless
public class PlayerService {

    @Inject
    EventStore store;

    @Inject
    EventRepository repository;

    public Player create() {
        Set<Player> players = repository.findAllPlayers();
        Long maxId = 0L;
        if (!players.isEmpty()) {
            maxId = players.parallelStream()
                    .map(Player::getId)
                    .max(Long::compareTo)
                    .get();
        }

        Long nextId = 0L;
        if (maxId != null) {
            nextId = maxId + 1;
        }

        Player player = new Player(new ArrayList<>());
        player.create(nextId);
        store.appendToStream(new EventIdentity(Player.class, nextId), 0L, player.getChanges());
        return player;
    }

    public void delete(Long id) {
        EventStream stream = store.loadEventStream(new EventIdentity(Player.class, id));
        if (stream.isEmpty()) {
            return;
        }
        Player player = new Player(stream.getEvents());
        player.delete();
        store.appendToStream(new EventIdentity(Player.class, id), stream.getVersion(), player.getChanges());
    }

    public Player changeFirstName(Long id, String firstName) {
        EventStream stream = store.loadEventStream(new EventIdentity(Player.class, id));
        Player player = new Player(stream.getEvents());
        player.changeFirstName(firstName);
        store.appendToStream(new EventIdentity(Player.class, id), stream.getVersion(), player.getChanges());
        return player;
    }

    public Player changeLastName(Long id, String lastName) {
        EventStream stream = store.loadEventStream(new EventIdentity(Player.class, id));
        Player player = new Player(stream.getEvents());
        player.changeLastName(lastName);
        store.appendToStream(new EventIdentity(Player.class, id), stream.getVersion(), player.getChanges());
        return player;
    }

}
