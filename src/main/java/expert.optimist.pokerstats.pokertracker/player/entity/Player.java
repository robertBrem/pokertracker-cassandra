package expert.optimist.pokerstats.pokertracker.player.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "players", schema = "pokertracker@cassandra")
@NamedQueries(@NamedQuery(name = "Player.findAll", query = "SELECT p FROM Player p"))
public class Player {

    @Id
    @TableGenerator(name = "id_gen", allocationSize = 1)
    @GeneratedValue(generator = "id_gen", strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

}
