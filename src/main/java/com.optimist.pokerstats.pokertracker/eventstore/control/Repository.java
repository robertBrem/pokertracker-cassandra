package com.optimist.pokerstats.pokertracker.eventstore.control;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.optimist.pokerstats.pokertracker.eventstore.entity.DataWithVersion;

import javax.inject.Inject;
import javax.json.JsonArray;
import java.util.ArrayList;
import java.util.List;

public class Repository {

    @Inject
    Session session;

    public List<DataWithVersion> readRecords(String name, Long version, Integer maxCount) {
        String readSql = "select DATA, VERSION from EVENTS" +
                " where NAME = :name and VERSION > :version"
//                +
//                " order by VERSION" +
//                " limit 0, :maxCount"
                + " allow filtering";
        PreparedStatement readStmt = session.prepare(readSql);
        BoundStatement readRecords = readStmt.bind()
                .setString("name", "'" + name + "'")
                .setLong("version", version)
//                .setInt("maxCount", maxCount)
                ;

        List<Row> records = session.execute(readRecords).all();
        return convertToDataWithVersions(records);
    }

    public List<DataWithVersion> readRecords(String name) {
        String readSql = "select DATA, VERSION from EVENTS" +
                " where NAME = :name"
//                +
//                " order by VERSION" +
//                " limit 0, :maxCount"
                + " allow filtering";
        PreparedStatement readStmt = session.prepare(readSql);
        BoundStatement readRecords = readStmt.bind()
                .setString("name", "'" + name + "'")
//                .setInt("maxCount", maxCount)
                ;

        List<Row> records = session.execute(readRecords).all();
        return convertToDataWithVersions(records);
    }

    private List<DataWithVersion> convertToDataWithVersions(List<Row> records) {
        List<DataWithVersion> result = new ArrayList<>();
        for (Row row : records) {
            result.add(new DataWithVersion(row.getLong("VERSION"), row.getString("DATA")));
        }

        return result;
    }

    // TODO ID
    public Long getNextId() {
        String versionSql = "select ID from EVENTS";
        PreparedStatement versionStmt = session.prepare(versionSql);
        BoundStatement readVersion = versionStmt.bind();
        Long id = null;
        List<Row> all = session.execute(readVersion).all();
        if (all == null || all.isEmpty()) {
            id = 0L;
        } else {
            id = all.stream()
                    .map(row -> row.getLong("ID"))
                    .max(Long::compareTo)
                    .get() + 1;
        }
        return id;
    }

    public void append(String name, JsonArray data, Long expectedVersion) {
        String versionSql = "select VERSION from EVENTS" +
                " where NAME = :name" +
                " allow filtering";
        PreparedStatement versionStmt = session.prepare(versionSql);
        BoundStatement readVersion = versionStmt.bind()
                .setString("name", "'" + name + "'");
        Long version = null;
        List<Row> all = session.execute(readVersion).all();
        if (all == null || all.isEmpty()) {
            version = 0L;
        } else {
            version = all.stream()
                    .map(row -> row.getLong("VERSION"))
                    .max(Long::compareTo)
                    .get();
        }
        if (!version.equals(expectedVersion)) {
            throw new RuntimeException("AppendConcurrentException, version does not match!");
        }
        Long newVersion = version + 1;
        String insertSql = "insert into EVENTS (ID, NAME, VERSION, DATA)" +
                " values (:id, :name, :version, :data)";
        PreparedStatement insertStmt = session.prepare(insertSql);
        BoundStatement insert = insertStmt.bind()
                .setLong("id", getNextId())
                .setString("name", "'" + name + "'")
                .setLong("version", newVersion)
                .setString("data", data.toString());
        session.execute(insert);
    }
}
