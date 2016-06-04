package com.optimist.pokerstats.pokertracker.player.entity;

import com.optimist.pokerstats.pokertracker.eventstore.control.CoreEvent;
import com.optimist.pokerstats.pokertracker.player.events.PlayerCreated;
import com.optimist.pokerstats.pokertracker.player.events.PlayerDeleted;
import com.optimist.pokerstats.pokertracker.player.events.PlayerFirstNameChanged;
import com.optimist.pokerstats.pokertracker.player.events.PlayerLastNameChanged;
import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(of = {"id"})
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Player {
    private Long id;
    private String firstName;
    private String lastName;

    @XmlTransient
    private final List<CoreEvent> changes = new ArrayList<>();

    public Player(List<CoreEvent> events) {
        for (CoreEvent event : events) {
            mutate(event);
        }
    }

    public void changeFirstName(String firstName) {
        apply(new PlayerFirstNameChanged(id, firstName));
    }

    public void changeLastName(String lastName) {
        apply(new PlayerLastNameChanged(id, lastName));
    }

    public void create(Long id) {
        apply(new PlayerCreated(id));
    }

    public void delete() {
        apply(new PlayerDeleted(id));
    }

    public void mutate(CoreEvent event) {
        when(event);
    }

    public void apply(CoreEvent event) {
        changes.add(event);
        mutate(event);
    }

    public void when(CoreEvent event) {
        if (event instanceof PlayerCreated) {
            this.id = event.getId();
        } else if (event instanceof PlayerFirstNameChanged) {
            this.firstName = ((PlayerFirstNameChanged) event).getFirstName();
        } else if (event instanceof PlayerLastNameChanged) {
            this.lastName = ((PlayerLastNameChanged) event).getLastName();
        }
    }

    public String getFormattedName() {
        return firstName + " " + lastName;
    }

}
