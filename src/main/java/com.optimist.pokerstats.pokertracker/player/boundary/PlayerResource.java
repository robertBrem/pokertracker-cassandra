package com.optimist.pokerstats.pokertracker.player.boundary;

import com.airhacks.porcupine.execution.boundary.Dedicated;
import com.optimist.pokerstats.pokertracker.account.boundary.AccountService;
import com.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import com.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDateTime;
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
    AccountService accountPositionService;

    @Inject
    @Dedicated
    ExecutorService threadPool;

    @GET
    public void getAll(@Suspended AsyncResponse response) {
        CompletableFuture
                .supplyAsync(service::findAllAsGenericEntities, threadPool)
                .thenAccept(response::resume);
    }

    @POST
    public void save(@Suspended AsyncResponse response, @Context UriInfo info, Player player) {
        Player saved = service.create();
        if (player.getFirstName() != null) {
            saved = service.changeFirstName(saved.getId(), player.getFirstName());
        }
        if (player.getLastName() != null) {
            saved = service.changeLastName(saved.getId(), player.getLastName());
        }
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
        if (player.getFirstName() != null) {
            player = service.changeFirstName(player.getId(), player.getFirstName());
        }
        if (player.getLastName() != null) {
            player = service.changeLastName(player.getId(), player.getLastName());
        }
        URI uri = info.getAbsolutePathBuilder().build();
        response.resume(Response.ok(uri).entity(player).build());
    }

    @POST
    @Path("{id}/accountpositions")
    public void createAccountPoisition(@Suspended AsyncResponse response, @Context UriInfo info, @PathParam("id") Long id, AccountPosition position) {
        AccountPosition saved = accountPositionService.create();
        Long possitionId = saved.getId();
        saved = accountPositionService.changeCreationDate(possitionId, LocalDateTime.now());
        if (position.getPlayerId() != null) {
            saved = accountPositionService.changePlayerId(possitionId, position.getPlayerId());
        }
        if (position.getAmount() != null) {
            saved = accountPositionService.changeAmount(possitionId, position.getAmount());
        }
        if (position.getCurrency() != null) {
            saved = accountPositionService.changeCurrency(possitionId, position.getCurrency());
        }
        URI uri = info.getAbsolutePathBuilder().path("/" + possitionId).build();
        response.resume(Response.created(uri).entity(saved).build());
    }

    @GET
    @Path("{id}/accountpositions")
    public void getAccountPositionsForPlayer(@Suspended AsyncResponse response, @PathParam("id") Long id) {
        GenericEntity<List<AccountPosition>> positions = accountPositionService.findByPlayerIdAsGenericEntities(id);
        response.resume(positions);
    }

//    @GET
//    @Path("{id}/accounthistory")
//    public void getAccountHistoryForPlayer(@Suspended AsyncResponse response, @PathParam("id") Long id, @QueryParam("summedUp") Boolean summedUp) {
//        response.resume(accountService.getHistoryForPlayerAsJson(id, summedUp, ChronoUnit.MINUTES));
//    }
//
//    @GET
//    @Path("accounthistory")
//    public void getAccountHistory(@Suspended AsyncResponse response, @QueryParam("summedUp") Boolean summedUp, @QueryParam("timeUnit") String timeUnitText) {
//        ChronoUnit timeUnit = ChronoUnit.MINUTES;
//        if (timeUnitText != null && !timeUnitText.isEmpty()) {
//            timeUnit = ChronoUnit.valueOf(timeUnitText);
//        }
//        response.resume(accountService.getHistoryAsJsonArray(summedUp, timeUnit));
//    }
//
//
//    @GET
//    @Path("{id}/balance")
//    public void getBalance(@Suspended AsyncResponse response, @PathParam("id") Long id) {
//        List<AccountPosition> positions = accountService.findByPlayerId(id);
//        Long balance = positions.stream()
//                .map(AccountPosition::getAmount)
//                .reduce(0L, Long::sum);
//        JsonObject balanceJson = Json.createObjectBuilder()
//                .add("value", balance)
//                .add("currency", "CHF")
//                .build();
//        response.resume(balanceJson);
//    }
//
//    @GET
//    @Path("{id}/accountpositions/{accountPositionId}")
//    public void getAccountPositions(@Suspended AsyncResponse response, @PathParam("id") Long id, @PathParam("accountPositionId") Long accountPositionId) {
//        response.resume(accountService.findById(accountPositionId));
//    }


}
