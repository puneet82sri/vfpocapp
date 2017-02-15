package com.vf.rest.service;

import com.vodafone.online.eserv.api.dynamiclanding.GetBalanceAPI;
import com.vodafone.online.eserv.services.request.getbalance.GetBalanceRequestDTO;

public class TestJTC {
    public TestJTC() {
        super();
    }
    
    public static void main(String[] args) {
        
        
        GetBalanceRequestDTO dto = GetBalanceAPI.getBalanceRequestAPI("", "", "");
        
        System.out.println("dto :: " + dto);
    }
}
