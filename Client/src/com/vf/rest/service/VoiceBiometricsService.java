package com.vf.rest.service;

import com.poc.vi.main.VoiceVerifier;

import com.vodafone.online.eserv.framework.coherence.client.ProductSchemaCacheClient;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapBusinessException;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapSystemException;
import com.vodafone.online.eserv.framework.utility.exception.CoherencePushException;
import com.vodafone.online.eserv.services.request.getbalance.GetBalanceRequestDTO;
import com.vodafone.online.eserv.services.response.getbalance.GetBalanceResponseDTO;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


@Path("/voicebio")
public class VoiceBiometricsService {
    public VoiceBiometricsService() {
        super();
    }
    
    
    @GET
    @Path("auth/{uniqueId}/{email}/{pwd}")
    public String authenticate(@PathParam("uniqueId") String uniqueId, @PathParam("email") String email, @PathParam("pwd") String pwd) {
        
        long startTime = System.currentTimeMillis();
        
        VoiceVerifier voiceVerifier = new VoiceVerifier();
        
        String status = voiceVerifier.authentication(VoiceVerifier.EMAIL, VoiceVerifier.PWD, "C:\\voiceBiometrics\\myvoice.wav", "70", "en-GB");
        
      
        long totalTime = (System.currentTimeMillis() - startTime)/1000;
        System.out.println("status : " + status + " Total execution time : " + totalTime);
        
        if (status!=null && status.contains("SUC")) {
            
            Map userData = null;
            try {
                userData =
                        (Map)ProductSchemaCacheClient.get("SessionCache", uniqueId);
            } catch (CoherencePushException e) {
                e.printStackTrace();
            }

            System.out.println("userData : " + userData);
            if (userData == null)
                userData = new HashMap();
            userData.put(OtacService.Authenticated, true);


            try {
                ProductSchemaCacheClient.put("SessionCache", uniqueId, userData);
            } catch (CoherencePushException e) {
            }
        }
    
    return status;
    }
}
