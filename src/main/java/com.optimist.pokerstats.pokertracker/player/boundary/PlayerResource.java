package com.optimist.pokerstats.pokertracker.player.boundary;

import com.airhacks.porcupine.execution.boundary.Dedicated;
import com.optimist.pokerstats.pokertracker.account.boundary.AccountService;
import com.optimist.pokerstats.pokertracker.account.entity.AccountPosition;
import com.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

@PermitAll
@Stateless
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

    @RolesAllowed("admin")
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

    @RolesAllowed("admin")
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

    @RolesAllowed("admin")
    @DELETE
    @Path("{id}")
    public void delete(@Suspended AsyncResponse response, @PathParam("id") Long id) {
        service.delete(id);
        response.resume(Response.noContent().build());
    }

    @RolesAllowed("admin")
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

}
