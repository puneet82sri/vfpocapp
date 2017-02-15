package com.vf.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@Path("packages")
public class PromotionFetcher {
    public PromotionFetcher() {
        super();
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/costgreater")
        public String getBalanceToString(@QueryParam("cost") long cost) {        
            return "Balance of " + cost + " is 5.40";
        }
}
