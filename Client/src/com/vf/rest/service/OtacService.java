package com.vf.rest.service;

import com.vodafone.online.eserv.framework.coherence.client.ProductSchemaCacheClient;

import com.vodafone.online.eserv.framework.utility.exception.CoherencePushException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


@Path("otac")
public class OtacService {
    
    public static final String OTAC = "otac";
    public static final String Authenticated = "isAuthenticated";
    public OtacService() {
        super();
    }
    
    @GET
    @Path("/validate/{uniqueId}/{otac}")
    public String validateOtac(@PathParam("uniqueId") String uniqueId, @PathParam("otac") String otac) {

//        vodafonelo
        System.out.println("validateOtac : " + uniqueId + " : " + otac);
        Map userData = null;
//        String otacSaved = null;

        try {
            userData =
                    (Map)ProductSchemaCacheClient.get("SessionCache", uniqueId);
        } catch (CoherencePushException e) {
            e.printStackTrace();
        }

        System.out.println("userData : " + userData);

        if (userData!= null && userData.get("otac")!= null && otac.equalsIgnoreCase(userData.get(OTAC).toString()))
        {
            userData.put(Authenticated, true);
            try {
                ProductSchemaCacheClient.put("SessionCache", uniqueId, userData);
            } catch (CoherencePushException e) {
            }
            return "true";
        }
        else
            return "false";
    }
    
    @GET
    @Path("/save/{uniqueId}/{otac}")
    public String saveOtac(@PathParam("uniqueId")
        String uniqueId, @PathParam("otac")
        String otac) {
        String status = "true";

        try {
            
           Map userData =
                    (Map)ProductSchemaCacheClient.get("SessionCache", uniqueId);
            
            if (userData == null) {
                userData = new HashMap();
            }
            
            userData.put(OTAC, otac);
            ProductSchemaCacheClient.put("SessionCache", uniqueId, userData);
            System.out.println("saveOtac userData : " + userData);
            System.out.println("SessionCache size : " +
                               ProductSchemaCacheClient.getCacheSize("SessionCache"));

        } catch (CoherencePushException e) {
            e.printStackTrace();
            status = "false";
        }
        return status;
    }
    
    @GET
    @Path("/isAuthenticated/{uniqueId}")
    public String isAuthenticated(@PathParam("uniqueId")
        String uniqueId) {
        String status = "false";

        try {
            
           Map userData =
                    (Map)ProductSchemaCacheClient.get("SessionCache", uniqueId);
            
            if (userData!= null && userData.get(Authenticated)!= null && "true".equalsIgnoreCase(userData.get(Authenticated).toString())) {
                status = "true";
            }

        } catch (CoherencePushException e) {
            e.printStackTrace();
        }
        return status;
    }
    
}
