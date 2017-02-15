package com.vf.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/gis")
public class GISService {
    public GISService() {
        super();
    }
    
    
    @GET
    @Path("oob/{accountID}/{billingProfileId}")
    public String getOutOfBundleCharges(@PathParam("accountID") String accountID, @PathParam("billingProfileId") String billingProfileId) {
        
        
        return "";
    }
}
