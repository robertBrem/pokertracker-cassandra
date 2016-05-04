package expert.optimist.pokerstats.pokertracker.player.boundary;

import expert.optimist.pokerstats.pokertracker.player.entity.Player;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Path("players")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlayerResource {

    @Inject
    PlayerService service;

    @Resource
    ManagedExecutorService mes;

    @GET
    public void getAll(@Suspended AsyncResponse response) {
        CompletableFuture
                .supplyAsync(service::getAllPlayersFromDB, mes)
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
    public void find(@Suspended AsyncResponse response, @PathParam("id") Integer id) {
        response.resume(service.find(id));
    }

    @PUT
    @Path("{id}")
    public void update(@Suspended AsyncResponse response, @Context UriInfo info, @PathParam("id") Integer id, Player player) {
        player.setId(id);
        Player saved = service.save(player);
        URI uri = info.getAbsolutePathBuilder().build();
        response.resume(Response.ok(uri).entity(saved).build());
    }

    @DELETE
    @Path("{id}")
    public void delete(@Suspended AsyncResponse response, @PathParam("id") Integer id) {
        service.delete(id);
        response.resume(Response.noContent().build());
    }

}
