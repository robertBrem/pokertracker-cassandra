package com.optimist.pokerstats.pokertracker.eventstore.control;


import com.optimist.pokerstats.pokertracker.player.event.PlayerFirstNameChanged;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class JsonConverterTest {

    private JsonConverter converter;

    @Before
    public void setUpTest() {
        converter = new JsonConverter();
    }

    @Test
    public void convertToEventsHappyPath() {
        String given = "[ "
                + "{ \"name\": \"com.optimist.pokerstats.pokertracker.player.event.PlayerFirstNameChanged\", "
                + "\"id\": 2,  \"firstName\": \"Robert\"  } "
                + "]";

        List<CoreEvent> events = converter.convertToEvents(given);

        assertThat(events, is(notNullValue()));
        assertFalse(events.isEmpty());
        assertTrue(events.size() == 1);
        CoreEvent event = events.get(0);
        assertThat(event, instanceOf(PlayerFirstNameChanged.class));
        PlayerFirstNameChanged firstNameChanged = (PlayerFirstNameChanged) event;
        assertThat(firstNameChanged.getFirstName(), equalTo("Robert"));
    }

}
