package expert.optimist.pokerstats.pokertracker.account.boundary;

import expert.optimist.pokerstats.pokertracker.EntityManagerCreator;
import expert.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import expert.optimist.pokerstats.pokertracker.player.boundary.PlayerService;
import expert.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.GenericEntity;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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

    public LinkedHashMap<Date, Long> findByPlayerIdHistory(Long playerId) {
        List<AccountPosition> positions = findByPlayerId(playerId);
        Date start = positions.stream()
                .map(AccountPosition::getDate)
                .min(Date::compareTo)
                .get();
        Date end = positions.stream()
                .map(AccountPosition::getDate)
                .max(Date::compareTo)
                .get();
        Map<LocalDateTime, List<AccountPosition>> groupByDate = positions.stream()
                .collect(Collectors.groupingBy(AccountPosition::getMinuteRounded));

        LinkedHashMap<Date, Long> history = new LinkedHashMap<>();
        LocalDateTime startAsLDT = LocalDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault());
        LocalDateTime endAsLDT = LocalDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault());
        for (LocalDateTime current = startAsLDT; !current.isAfter(endAsLDT); current = current.plusMinutes(1L)) {
            LocalDateTime minuteRounded = current.truncatedTo(ChronoUnit.MINUTES);
            List<AccountPosition> positionsForMinute = groupByDate.get(minuteRounded);
            if (positionsForMinute == null) {
                positionsForMinute = new ArrayList<>();
            }
            Long balance = positionsForMinute.stream()
                    .map(AccountPosition::getAmount)
                    .reduce(0L, Long::sum);
            Date asDate = Date.from(minuteRounded.atZone(ZoneId.systemDefault()).toInstant());
            history.put(asDate, balance);
        }

        return history;
    }

    public LinkedHashMap<Date, Long> findByPlayerIdHistorySummedUp(Long playerId) {
        LinkedHashMap<Date, Long> history = findByPlayerIdHistory(playerId);
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

}
