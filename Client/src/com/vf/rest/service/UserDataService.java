package com.vf.rest.service;

import com.vodafone.online.eserv.framework.coherence.client.ProductSchemaCacheClient;

import com.vodafone.online.eserv.framework.utility.exception.CoherencePushException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


@Path("userdata")
public class UserDataService {
    
    public static final String OTAC = "otac";
    public static final String Authenticated = "isAuthenticated";
    public UserDataService() {
        super();
    }
    
   
    @GET
    @Path("/save/{uniqueId}/{key}/{value}")
    public String saveData(@PathParam("uniqueId")
        String uniqueId, @PathParam("key")
        String key, @PathParam("value")
        String value) {
        String status = "true";

        try {
            
           Map userData =
                    (Map)ProductSchemaCacheClient.get("SessionCache", uniqueId);
            
            if (userData == null) {
                userData = new HashMap();
            }
            
            userData.put(key, value);
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
    @Path("/get/{uniqueId}/{key}")
    public String getData(@PathParam("uniqueId")
        String uniqueId, @PathParam("key")
        String key) {
        String status = "null";

        System.out.println("uniqueId : " + uniqueId + " key : " + key);

        try {
            
           Map userData =
                    (Map)ProductSchemaCacheClient.get("SessionCache", uniqueId);
            if (userData != null && userData.get(key)!= null)
            {
            status = (String)userData.get(key);
            System.out.println("userData : " + userData);
            }

        } catch (CoherencePushException e) {
            e.printStackTrace();
        }
        return status;
    }
    
}
