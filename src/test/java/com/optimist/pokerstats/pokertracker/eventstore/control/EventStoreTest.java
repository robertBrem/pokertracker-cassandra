package com.optimist.pokerstats.pokertracker.eventstore.control;


import com.optimist.pokerstats.pokertracker.player.events.PlayerFirstNameChanged;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EventStoreTest {

    private EventStore store;

    @Before
    public void setUpTest() {
        store = new EventStore();
    }

    @Test
    public void convertToEventsHappyPath() {
        String given = "[ "
                + "{ \"name\": \"com.optimist.pokerstats.pokertracker.player.events.PlayerFirstNameChanged\", "
                + "\"id\": 2,  \"firstName\": \"Robert\"  } "
        + "]";

        List<CoreEvent> events = store.convertToEvents(given);

        assertThat(events, is(notNullValue()));
        assertFalse(events.isEmpty());
        assertTrue(events.size() == 1);
        CoreEvent event = events.get(0);
        assertThat(event, instanceOf(PlayerFirstNameChanged.class));
        PlayerFirstNameChanged firstNameChanged = (PlayerFirstNameChanged) event;
        assertThat(firstNameChanged.getFirstName(), equalTo("Robert"));
    }

}
