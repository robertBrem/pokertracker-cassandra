package expert.optimist.pokerstats.pokertracker.account.entity;

import expert.optimist.pokerstats.pokertracker.player.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ACCOUNT_POSITIONS", schema = "pokertracker@cassandra")
@NamedQueries({
        @NamedQuery(name = "AccountPositions.findAll", query = "SELECT ap FROM AccountPosition ap")
})
public class AccountPosition {

    @Id
    @TableGenerator(name = "id_gen", allocationSize = 1)
    @GeneratedValue(generator = "id_gen", strategy = GenerationType.TABLE)
    @Column(name = "ACCOUNT_POSITION_ID")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAYER_ID")
    @Column(name = "PLAYER_ID")
    private Player player;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "CREATION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public LocalDateTime getMinuteRounded() {
        Date date = getDate();
        LocalDateTime asLDT = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return asLDT.truncatedTo(ChronoUnit.MINUTES);
    }
}
