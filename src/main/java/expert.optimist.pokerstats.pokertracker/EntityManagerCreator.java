package expert.optimist.pokerstats.pokertracker;


import com.impetus.client.cassandra.common.CassandraConstants;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

@Stateless
public class EntityManagerCreator {

    private EntityManagerFactory emf;
    @Getter
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
        configOverrides.put(CassandraConstants.CQL_VERSION, CassandraConstants.CQL_VERSION_3_0);
        emf = Persistence.createEntityManagerFactory("cassandra", configOverrides);
        em = emf.createEntityManager();
    }

}
