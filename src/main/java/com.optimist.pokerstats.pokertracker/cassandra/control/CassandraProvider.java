package com.optimist.pokerstats.pokertracker.cassandra.control;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.optimist.pokerstats.pokertracker.EnvironmentVariableGetter;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class CassandraProvider {
    public static final String CASSANDRA_ADDRESS = "CASSANDRA_ADDRESS";
    public static final String KEYSPACE = "pokertracker";

    @Inject
    EnvironmentVariableGetter envGetter;

    private Session session;

    @PostConstruct
    public void init() {
        String address = "localhost";
        String cassandraEnv = envGetter.getEnv(CASSANDRA_ADDRESS);
        if (cassandraEnv != null && !cassandraEnv.isEmpty()) {
            address = cassandraEnv;
        }
        Cluster cluster = Cluster.builder()
                .addContactPoints(address)
                .build();
        session = cluster.connect(KEYSPACE);
    }

    @Produces
    public Session getSession() {
        return session;
    }

}
