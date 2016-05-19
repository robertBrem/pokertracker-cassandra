package expert.optimist.pokerstats.pokertracker.account.boundary;

import expert.optimist.pokerstats.pokertracker.EntityManagerCreator;
import expert.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import expert.optimist.pokerstats.pokertracker.player.boundary.PlayerService;
import expert.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.*;
import javax.persistence.EntityManager;
import javax.ws.rs.core.GenericEntity;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class AccountService {

    @Inject
    EntityManagerCreator emCreator;

    @Inject
    PlayerService playerService;

    EntityManager em;

    @PostConstruct
    public void setUp() {
        this.em = emCreator.getEm();
    }

    public GenericEntity<List<AccountPosition>> findByPlayerIdAsGenericEntities(Long playerId) {
        return new GenericEntity<List<AccountPosition>>(findByPlayerId(playerId)) {
        };
    }

    public JsonArray getHistoryAsJsonArray(Boolean summedUp, TemporalUnit groupUnit) {
        JsonArrayBuilder result = Json.createArrayBuilder();

        LinkedHashMap<Date, Long> allPositions = getHistory(getAllAccountPositions(), groupUnit);
        Date start = allPositions.keySet().stream()
                .min(Date::compareTo)
                .get();
        Date end = allPositions.keySet().stream()
                .max(Date::compareTo)
                .get();

        for (Player player : playerService.getAllPlayers()) {
            LinkedHashMap<Date, Long> history = getHistory(findByPlayerId(player.getId()), start, end, groupUnit);
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

    public LinkedHashMap<Date, Long> getHistory(List<AccountPosition> positions, TemporalUnit groupUnit) {
        Date start = positions.stream()
                .map(AccountPosition::getDate)
                .min(Date::compareTo)
                .get();
        Date end = positions.stream()
                .map(AccountPosition::getDate)
                .max(Date::compareTo)
                .get();
        return getHistory(positions, start, end, groupUnit);
    }

    public LinkedHashMap<Date, Long> getHistory(List<AccountPosition> positions, Date start, Date end, TemporalUnit groupUnit) {
        Map<LocalDateTime, List<AccountPosition>> groupByDate = positions.stream()
                .collect(Collectors.groupingBy(ap -> ap.getRounded(groupUnit)));

        LinkedHashMap<Date, Long> history = new LinkedHashMap<>();
        LocalDateTime startAsLDT = LocalDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault()).truncatedTo(groupUnit);
        LocalDateTime endAsLDT = LocalDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault()).truncatedTo(groupUnit);
        for (LocalDateTime current = startAsLDT; !current.isAfter(endAsLDT); current = current.plus(1L, groupUnit)) {
            LocalDateTime groupUnitRounded = current.truncatedTo(groupUnit);
            List<AccountPosition> positionsForGroupUnit = groupByDate.get(groupUnitRounded);
            if (positionsForGroupUnit == null) {
                positionsForGroupUnit = new ArrayList<>();
            }
            Long balance = positionsForGroupUnit.stream()
                    .map(AccountPosition::getAmount)
                    .reduce(0L, Long::sum);
            Date asDate = Date.from(groupUnitRounded.atZone(ZoneId.systemDefault()).toInstant());
            history.put(asDate, balance);
        }
        return history;
    }

    public LinkedHashMap<Date, Long> getSummedUp(LinkedHashMap<Date, Long> history) {
        LinkedHashMap<Date, Long> historySummedUp = new LinkedHashMap<>();
        Long currentTotalBalance = 0L;
        for (Date date : history.keySet()) {
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

    public LinkedHashMap<Date, Long> getHistoryForPlayer(Long id, Boolean summedUp, TemporalUnit groupUnit) {
        if (summedUp != null && summedUp) {
            return getSummedUp(getHistory(findByPlayerId(id), groupUnit));
        }
        return getHistory(findByPlayerId(id), groupUnit);
    }


    public JsonArray getHistoryForPlayerAsJson(Long id, Boolean summedUp, TemporalUnit groupUnit) {
        LinkedHashMap<Date, Long> history = getHistoryForPlayer(id, summedUp, groupUnit);
        return getJsonArray(history);
    }

    private JsonArray getJsonArray(LinkedHashMap<Date, Long> history) {
        JsonArrayBuilder historyAsJson = Json.createArrayBuilder();
        for (Date date : history.keySet()) {
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

    /**
     * Named Queries with parameters are not supported in Kundera for now.
     */
    public List<AccountPosition> findByPlayerId(Long playerId) {
        Player player = playerService.find(playerId);

        List<?> nativeQuery = em.createNativeQuery("SELECT \"ACCOUNT_POSITION_ID\", \"AMOUNT\", \"CURRENCY\", \"DATE\", \"CREATION\" FROM \"ACCOUNT_POSITIONS\" WHERE \"PLAYER_ID\" = " + playerId).getResultList();
        List<Map<String, Object>> nativeResult = (List<Map<String, Object>>) nativeQuery;
        List<AccountPosition> result = new ArrayList<>();
        for (Map<String, Object> position : nativeResult) {
            Long accountPositionId = (Long) position.get("ACCOUNT_POSITION_ID");
            Long amount = (Long) position.get("AMOUNT");
            String currency = (String) position.get("CURRENCY");
            Date date = (Date) position.get("DATE");
            Date creation = (Date) position.get("CREATION");
            result.add(new AccountPosition(accountPositionId, player, amount, currency, date, creation));
        }
        return result;
    }

    public AccountPosition findById(Long id) {
        return em.find(AccountPosition.class, id);
    }

    public AccountPosition save(AccountPosition position, Long playerId) {
        Player player = playerService.find(playerId);
        position.setPlayer(player);
        position.setTimestamp(new Date());
        position.setDate(new Date());
        return em.merge(position);
    }

    public GenericEntity<List<AccountPosition>> getAllAccountPositionsAsGenericEntity() {
        List<AccountPosition> positions = getAllAccountPositions();
        return new GenericEntity<List<AccountPosition>>(positions) {
        };
    }

    public List<AccountPosition> getAllAccountPositions() {
        return em.createNamedQuery("AccountPositions.findAll", AccountPosition.class).getResultList();
    }

}
