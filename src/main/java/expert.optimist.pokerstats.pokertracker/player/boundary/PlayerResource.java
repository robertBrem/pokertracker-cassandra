package expert.optimist.pokerstats.pokertracker.player.boundary;

import com.airhacks.porcupine.execution.boundary.Dedicated;
import expert.optimist.pokerstats.pokertracker.account.boundary.AccountService;
import expert.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import expert.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Path("players")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlayerResource {

    @Inject
    PlayerService service;

    @Inject
    AccountService accountService;

    @Inject
    @Dedicated
    ExecutorService threadPool;

    @GET
    public void getAll(@Suspended AsyncResponse response) {
        CompletableFuture
                .supplyAsync(service::getAllPlayersAsGenericEntity, threadPool)
                .thenAccept(response::resume);
    }

    @POST
    public void save(@Suspended AsyncResponse response, @Context UriInfo info, Player player) {
        Player saved = service.save(player);
        long id = saved.getId();
        URI uri = info.getAbsolutePathBuilder().path("/" + id).build();
        response.resume(Response.created(uri).entity(saved).build());
    }

    @GET
    @Path("{id}")
    public void find(@Suspended AsyncResponse response, @PathParam("id") Long id) {
        response.resume(service.find(id));
    }

    @PUT
    @Path("{id}")
    public void update(@Suspended AsyncResponse response, @Context UriInfo info, @PathParam("id") Long id, Player player) {
        player.setId(id);
        Player saved = service.save(player);
        URI uri = info.getAbsolutePathBuilder().build();
        response.resume(Response.ok(uri).entity(saved).build());
    }

    @POST
    @Path("{id}")
    public void createAccountPoisition(@Suspended AsyncResponse response, @Context UriInfo info, @PathParam("id") Long id, AccountPosition position) {
        AccountPosition saved = accountService.save(position, id);
        long possitionId = saved.getId();
        URI uri = info.getAbsolutePathBuilder().path("/accountpositions/" + possitionId).build();
        response.resume(Response.created(uri).entity(saved).build());
    }

    @GET
    @Path("{id}/accountpositions")
    public void getAccountPositionsForPlayer(@Suspended AsyncResponse response, @PathParam("id") Long id) {
        GenericEntity<List<AccountPosition>> positions = accountService.findByPlayerIdAsGenericEntities(id);
        response.resume(positions);
    }

    @GET
    @Path("{id}/accounthistory")
    public void getAccountHistoryForPlayer(@Suspended AsyncResponse response, @PathParam("id") Long id, @QueryParam("summedUp") Boolean summedUp) {
        response.resume(accountService.getHistoryForPlayerAsJson(id, summedUp, ChronoUnit.MINUTES));
    }

    @GET
    @Path("accounthistory")
    public void getAccountHistory(@Suspended AsyncResponse response, @QueryParam("summedUp") Boolean summedUp, @QueryParam("timeUnit") String timeUnitText) {
        ChronoUnit timeUnit = ChronoUnit.MINUTES;
        if (timeUnitText != null && !timeUnitText.isEmpty()) {
            timeUnit = ChronoUnit.valueOf(timeUnitText);
        }
        response.resume(accountService.getHistoryAsJsonArray(summedUp, timeUnit));
    }


    @GET
    @Path("{id}/balance")
    public void getBalance(@Suspended AsyncResponse response, @PathParam("id") Long id) {
        List<AccountPosition> positions = accountService.findByPlayerId(id);
        Long balance = positions.stream()
                .map(AccountPosition::getAmount)
                .reduce(0L, Long::sum);
        JsonObject balanceJson = Json.createObjectBuilder()
                .add("value", balance)
                .add("currency", "CHF")
                .build();
        response.resume(balanceJson);
    }

    @GET
    @Path("{id}/accountpositions/{accountPositionId}")
    public void getAccountPositions(@Suspended AsyncResponse response, @PathParam("id") Long id, @PathParam("accountPositionId") Long accountPositionId) {
        response.resume(accountService.findById(accountPositionId));
    }

    @DELETE
    @Path("{id}")
    public void delete(@Suspended AsyncResponse response, @PathParam("id") Long id) {
        service.delete(id);
        response.resume(Response.noContent().build());
    }

}
