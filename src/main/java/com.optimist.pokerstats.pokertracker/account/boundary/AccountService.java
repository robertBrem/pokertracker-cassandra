package com.optimist.pokerstats.pokertracker.account.boundary;

import com.optimist.pokerstats.pokertracker.InMemoryCache;
import com.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStore;
import com.optimist.pokerstats.pokertracker.eventstore.control.EventStream;
import com.optimist.pokerstats.pokertracker.eventstore.entity.EventIdentity;
import com.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.*;
import javax.ws.rs.core.GenericEntity;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class AccountService {

    @Inject
    EventStore store;

    @Inject
    InMemoryCache cache;

    public GenericEntity<Set<AccountPosition>> findAllAsGenericEntities() {
        return new GenericEntity<Set<AccountPosition>>(findAll()) {
        };
    }

    public Set<AccountPosition> findAll() {
        return new HashSet<>(cache.getAccountPositions().values());
    }

    public AccountPosition find(Long id) {
        return cache.getAccountPositions().get(id);
    }

    public AccountPosition create() {
        // TODO
        Set<Long> accountPositionIds = cache.getAccountPositions().keySet();
        Long maxId = 0L;
        if (!accountPositionIds.isEmpty()) {
            maxId = accountPositionIds.stream()
                    .max(Long::compareTo)
                    .get();
        }

        Long nextId = 0L;
        if (maxId != null) {
            nextId = maxId + 1;
        }

        AccountPosition accountPosition = new AccountPosition(new ArrayList<>());
        accountPosition.create(nextId);
        store.appendToStream(new EventIdentity(AccountPosition.class, nextId), 0L, accountPosition.getChanges());
        return accountPosition;
    }

    public AccountPosition changePlayerId(Long id, Long playerId) {
        EventStream stream = store.loadEventStream(new EventIdentity(AccountPosition.class, id));
        AccountPosition accountPosition = new AccountPosition(stream.getEvents());
        accountPosition.changePlayerId(playerId);
        store.appendToStream(new EventIdentity(AccountPosition.class, id), stream.getVersion(), accountPosition.getChanges());
        return accountPosition;
    }

    public AccountPosition changeAmount(Long id, Long amount) {
        EventStream stream = store.loadEventStream(new EventIdentity(AccountPosition.class, id));
        AccountPosition accountPosition = new AccountPosition(stream.getEvents());
        accountPosition.changeAmount(amount);
        store.appendToStream(new EventIdentity(AccountPosition.class, id), stream.getVersion(), accountPosition.getChanges());
        return accountPosition;
    }

    public AccountPosition changeCurrency(Long id, String currency) {
        EventStream stream = store.loadEventStream(new EventIdentity(AccountPosition.class, id));
        AccountPosition accountPosition = new AccountPosition(stream.getEvents());
        accountPosition.changeCurrency(currency);
        store.appendToStream(new EventIdentity(AccountPosition.class, id), stream.getVersion(), accountPosition.getChanges());
        return accountPosition;
    }

    public AccountPosition changeCreationDate(Long id, LocalDateTime creationDate) {
        EventStream stream = store.loadEventStream(new EventIdentity(AccountPosition.class, id));
        AccountPosition accountPosition = new AccountPosition(stream.getEvents());
        accountPosition.changeCreationDate(creationDate);
        store.appendToStream(new EventIdentity(AccountPosition.class, id), stream.getVersion(), accountPosition.getChanges());
        return accountPosition;
    }

    public GenericEntity<List<AccountPosition>> findByPlayerIdAsGenericEntities(Long playerId) {
        return new GenericEntity<List<AccountPosition>>(findByPlayerId(playerId)) {
        };
    }

    public List<AccountPosition> findByPlayerId(Long playerId) {
        return cache.getAccountPositions().values().stream()
                .filter(ap -> playerId != null && playerId.equals(ap.getPlayerId()))
                .collect(Collectors.toList());
    }

    public JsonArray getHistoryForPlayerAsJson(Long id, Boolean summedUp, TemporalUnit groupUnit) {
        LinkedHashMap<LocalDateTime, Long> history = getHistoryForPlayer(id, summedUp, groupUnit);
        return getJsonArray(history);
    }

    public LinkedHashMap<LocalDateTime, Long> getHistoryForPlayer(Long id, Boolean summedUp, TemporalUnit groupUnit) {
        if (summedUp != null && summedUp) {
            return getSummedUp(getHistory(findByPlayerId(id), groupUnit));
        }
        return getHistory(findByPlayerId(id), groupUnit);
    }

    private JsonArray getJsonArray(LinkedHashMap<LocalDateTime, Long> history) {
        JsonArrayBuilder historyAsJson = Json.createArrayBuilder();
        for (LocalDateTime date : history.keySet()) {
            JsonObjectBuilder entryBuilder = Json.createObjectBuilder();
            entryBuilder.add("date", date.toString());
            if (history.get(date) == null) {
                entryBuilder.add("balance", JsonValue.NULL);
            } else {
                entryBuilder.add("balance", history.get(date));
            }
            JsonObject entry = entryBuilder.build();
            historyAsJson.add(entry);
        }
        return historyAsJson.build();
    }

    public LinkedHashMap<LocalDateTime, Long> getHistory(List<AccountPosition> positions, TemporalUnit groupUnit) {
        LocalDateTime start = positions.stream()
                .map(AccountPosition::getCreationDate)
                .min(LocalDateTime::compareTo)
                .get();
        LocalDateTime end = positions.stream()
                .map(AccountPosition::getCreationDate)
                .max(LocalDateTime::compareTo)
                .get();
        return getHistory(positions, start, end, groupUnit);
    }

    public LinkedHashMap<LocalDateTime, Long> getHistory(List<AccountPosition> positions, LocalDateTime start, LocalDateTime end, TemporalUnit groupUnit) {
        Map<LocalDateTime, List<AccountPosition>> groupByDate = positions.stream()
                .collect(Collectors.groupingBy(ap -> ap.getRounded(groupUnit)));

        LinkedHashMap<LocalDateTime, Long> history = new LinkedHashMap<>();
        for (LocalDateTime current = start; !current.isAfter(end); current = current.plus(1L, groupUnit)) {
            LocalDateTime groupUnitRounded = current.truncatedTo(groupUnit);
            List<AccountPosition> positionsForGroupUnit = groupByDate.get(groupUnitRounded);
            if (positionsForGroupUnit == null) {
                positionsForGroupUnit = new ArrayList<>();
            }
            Long balance = positionsForGroupUnit.stream()
                    .map(AccountPosition::getAmount)
                    .reduce(0L, Long::sum);
            history.put(current, balance);
        }
        return history;
    }

    public LinkedHashMap<LocalDateTime, Long> getSummedUp(LinkedHashMap<LocalDateTime, Long> history) {
        LinkedHashMap<LocalDateTime, Long> historySummedUp = new LinkedHashMap<>();
        Long currentTotalBalance = 0L;
        for (LocalDateTime date : history.keySet()) {
            Long dateBalance = history.get(date);
            if (dateBalance == null || dateBalance.equals(0L)) {
                historySummedUp.put(date, null);
            } else {
                currentTotalBalance += dateBalance;
                historySummedUp.put(date, currentTotalBalance);
            }
        }
        return historySummedUp;
    }

    public JsonArray getHistoryAsJsonArray(Boolean summedUp, TemporalUnit groupUnit) {
        JsonArrayBuilder result = Json.createArrayBuilder();

        LinkedHashMap<LocalDateTime, Long> allPositions = getHistory(getAllAccountPositions(), groupUnit);
        LocalDateTime start = allPositions.keySet().stream()
                .min(LocalDateTime::compareTo)
                .get();
        LocalDateTime end = allPositions.keySet().stream()
                .max(LocalDateTime::compareTo)
                .get();

        for (Player player : cache.getPlayers().values()) {
            LinkedHashMap<LocalDateTime, Long> history = getHistory(findByPlayerId(player.getId()), start, end, groupUnit);
            if (summedUp != null && summedUp) {
                history = getSummedUp(history);
            }
            JsonObject entry = Json.createObjectBuilder()
                    .add("playerName", player.getFormattedName())
                    .add("history", getJsonArray(history))
                    .build();
            result.add(entry);
        }
        return result.build();
    }

    public GenericEntity<List<AccountPosition>> getAllAccountPositionsAsGenericEntity() {
        List<AccountPosition> positions = getAllAccountPositions();
        return new GenericEntity<List<AccountPosition>>(positions) {
        };
    }

    public List<AccountPosition> getAllAccountPositions() {
        return new ArrayList<>(cache.getAccountPositions().values());
    }
}
