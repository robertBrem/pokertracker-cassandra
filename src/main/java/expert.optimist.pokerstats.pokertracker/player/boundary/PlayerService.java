package expert.optimist.pokerstats.pokertracker.player.boundary;

import expert.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.core.GenericEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class PlayerService {

    private EntityManagerFactory emf;
    private EntityManager em;

    @PostConstruct
    public void setUp() {
        Map<String, String> env = System.getenv();
        Map<String, Object> configOverrides = new HashMap<>();
        for (String envName : env.keySet()) {
            if (envName.contains("CASSANDRA_IP")) {
                System.out.println("found = " + env.get(envName));
                configOverrides.put("kundera.nodes", env.get(envName));
            }
        }
        emf = Persistence.createEntityManagerFactory("cassandra", configOverrides);
        em = emf.createEntityManager();
    }


    public Player save(Player player) {
        return em.merge(player);
    }

    public Player find(Integer id) {
        return em.find(Player.class, id);
    }

    public void delete(Integer id) {
        Player reference = em.find(Player.class, id);
        if (reference == null) {
            return;
        }
        this.em.remove(reference);
    }

    public GenericEntity<List<Player>> getAllPlayersFromDB() {
        List<Player> players = em.createNamedQuery("Player.findAll", Player.class).getResultList();
        return new GenericEntity<List<Player>>(players) {
        };
    }

}
