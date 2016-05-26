package com.optimist.pokerstats.pokertracker.player.boundary;

import com.optimist.pokerstats.pokertracker.InMemoryCache;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStream;
import com.optimist.pokerstats.pokertracker.eventstore.entity.EventIdentity;
import com.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.GenericEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Stateless
public class PlayerService {

    @Inject
    EventStore store;

    @Inject
    InMemoryCache cache;

    public GenericEntity<Set<Player>> findAllAsGenericEntities() {
        return new GenericEntity<Set<Player>>(findAll()) {
        };
    }

    public Set<Player> findAll() {
        return new HashSet<>(cache.getPlayers().values());
    }

    public Player find(Long id) {
        return cache.getPlayers().get(id);
    }


    public Player create() {
        Set<Long> playerIds = cache.getPlayers().keySet();
        Long maxId = 0L;
        if (!playerIds.isEmpty()) {
            maxId = playerIds.stream()
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
