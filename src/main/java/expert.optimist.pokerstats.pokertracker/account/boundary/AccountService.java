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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
