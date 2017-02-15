package com.vf.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@Path("resthello")
public class RestHello {
    public RestHello() {
        super();
    }
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String sayHello() {
        return "Hey its puneet.. bad timing is badd!!";
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/balance")
        public String getBalanceToString(@QueryParam("id") long id) {        
            return "Balance of " + id + " is 5.40";
        }
}
