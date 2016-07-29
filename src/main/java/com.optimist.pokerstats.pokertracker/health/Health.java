package com.optimist.pokerstats.pokertracker.health;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("health")
public class Health {

    @GET
    public String getHealth() {
        return "running";
    }

}
