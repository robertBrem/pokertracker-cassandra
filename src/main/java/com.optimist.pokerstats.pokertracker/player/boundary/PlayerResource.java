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
import java.net.URISyntaxException;
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
        response.resume(Response.created(updatePath(uri)).entity(saved).build());
    }

    public URI updatePath(URI uri) {
        URI newUri = setUriPort(uri, 8383);
        newUri = setFirstPathElement(newUri, "pokertracker-query");
        return newUri;
    }

    public URI setFirstPathElement(URI uri, String firstPathElement) {
        String path = uri.getPath();
        String[] pathElements = path.split("/");
        if (pathElements.length < 2) {
            return uri;
        }

        pathElements[1] = firstPathElement;
        String newPath = "";
        for (String pathElement : pathElements) {
            newPath += pathElement + "/";
        }
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), newPath, uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public URI setUriPort(URI uri, int port) {
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), port, uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
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
        response.resume(Response.created(updatePath(uri)).entity(saved).build());
    }

}
