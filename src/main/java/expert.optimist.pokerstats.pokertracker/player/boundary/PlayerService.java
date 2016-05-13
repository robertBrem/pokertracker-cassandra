package expert.optimist.pokerstats.pokertracker.player.boundary;

import expert.optimist.pokerstats.pokertracker.EntityManagerCreator;
import expert.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.GenericEntity;
import java.util.List;

@Stateless
public class PlayerService {

    @Inject
    EntityManagerCreator emCreator;

    EntityManager em;

    @PostConstruct
    public void setUp() {
        this.em = emCreator.getEm();
    }

    public Player save(Player player) {
        return em.merge(player);
    }

    public Player find(Long id) {
        return em.find(Player.class, id);
    }

    public void delete(Long id) {
        Player reference = em.find(Player.class, id);
        if (reference == null) {
            return;
        }
        this.em.remove(reference);
    }

    public GenericEntity<List<Player>> getAllPlayersFromDB() {
        List<Player> players = em.createNamedQuery("Players.findAll", Player.class).getResultList();
        return new GenericEntity<List<Player>>(players) {
        };
    }

}
