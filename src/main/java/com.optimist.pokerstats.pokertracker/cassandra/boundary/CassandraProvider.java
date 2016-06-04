package com.optimist.pokerstats.pokertracker.cassandra.boundary;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.optimist.pokerstats.pokertracker.EnvironmentVariableGetter;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class CassandraProvider {
    public static final String CASSANDRA_IP = "CASSANDRA_IP";
    public static final String KEYSPACE = "pokertracker";

    @Inject
    EnvironmentVariableGetter envGetter;

    private Session session;

    @PostConstruct
    public void init() {
        String serverIP = "localhost";
        String cassandraEnv = envGetter.getEnv(CASSANDRA_IP);
        if (cassandraEnv != null && !cassandraEnv.isEmpty()) {
            serverIP = cassandraEnv;
        }
        Cluster cluster = Cluster.builder()
                .addContactPoints(serverIP)
                .build();
        session = cluster.connect(KEYSPACE);
    }

    @Produces
    public Session getSession(InjectionPoint ip) {
        return session;
    }

}
