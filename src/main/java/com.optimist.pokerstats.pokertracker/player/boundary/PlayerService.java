package com.optimist.pokerstats.pokertracker.player.boundary;

import com.optimist.pokerstats.pokertracker.InMemoryCache;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStream;
import com.optimist.pokerstats.pokertracker.eventstore.control.Repository;
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

    @Inject
    Repository repository;

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
        // TODO
        Long newId = repository.getNextId();
        Player player = new Player(new ArrayList<>());
        player.create(newId);
        store.appendToStream(new EventIdentity(Player.class, newId), 0L, player.getChanges());
        return player;
    }

    public Player changeFirstName(Long id, String firstName) {
        EventStream stream = store.loadEventStream(new EventIdentity(Player.class, id));
        Player player = new Player(stream.getEvents());
        player.changeFirstName(firstName);
        store.appendToStream(new EventIdentity(Player.class, id), stream.getVersion(), player.getChanges());
        return player;
    }

}
