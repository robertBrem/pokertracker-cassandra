package expert.optimist.pokerstats.pokertracker.player.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "PLAYERS", schema = "pokertracker@cassandra")
@NamedQueries(
        @NamedQuery(name = "Players.findAll", query = "SELECT p FROM Player p")
)
public class Player {

    @Id
    @TableGenerator(name = "id_gen", allocationSize = 1)
    @GeneratedValue(generator = "id_gen", strategy = GenerationType.TABLE)
    @Column(name = "PLAYER_ID")
    private Long id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    public String getFormattedName() {
        return firstName + " " + lastName;
    }

}
