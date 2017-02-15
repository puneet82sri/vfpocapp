package com.vf.rest.service;

import com.vodafone.online.eserv.api.dynamiclanding.GetBalanceAPI;
import com.vodafone.online.eserv.api.ebillingv2.GetInvoiceListAPI;
import com.vodafone.online.eserv.constants.EBillingConstants;
import com.vodafone.online.eserv.ebillingv2.GILRequestData;
import com.vodafone.online.eserv.ebillingv2.InvoiceDetails;
import com.vodafone.online.eserv.framework.coherence.client.ProductSchemaCacheClient;
import com.vodafone.online.eserv.framework.jtc.dto.BaseVodafoneDTO;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapBusinessException;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapSystemException;
import com.vodafone.online.eserv.framework.jtc.factory.ProxyServiceFactory;
import com.vodafone.online.eserv.framework.utility.exception.CoherencePushException;
import com.vodafone.online.eserv.framework.utility.logger.VodafoneLogger;
import com.vodafone.online.eserv.services.request.getbalance.GetBalanceRequestDTO;

import com.vodafone.online.eserv.services.request.getinvoicelist.GetInvoiceListRequestDTO;
import com.vodafone.online.eserv.services.request.serviceCaseList.QueryCriteriaDTO;
import com.vodafone.online.eserv.services.request.serviceCaseList.QueryDTO;
import com.vodafone.online.eserv.services.request.serviceCaseList.QueryExpressionDTO;
import com.vodafone.online.eserv.services.request.serviceCaseList.ValueExpressionDTO;
import com.vodafone.online.eserv.services.response.getbalance.GetBalanceResponseDTO;

import java.math.BigDecimal;

import java.math.BigInteger;

import java.math.RoundingMode;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@Path("/gil")
public class GetInvoiceListRestService {
    
        @GET
        @Path("bill/{accountID}/{billingProfileId}")
    public String getBill(@PathParam("accountID") String accountID, @PathParam("billingProfileId") String billingProfileId) {

        String response = "";
        GILRequestData giplRequestData = new GILRequestData();
        giplRequestData.setBillingProfID("billingprofile-puneet");
        giplRequestData.setAccountID("accountId-puneet");
        giplRequestData.setMaxItems(100);
        giplRequestData.setResponseCode("FR");


        List<InvoiceDetails> invoiceList = null;

        try {
            invoiceList = GetInvoiceListAPI.getinvoiceList(giplRequestData);
        } catch (Exception e) {
            
            e.printStackTrace();
        } 
        
        
        InvoiceDetails currentBill = null;
        InvoiceDetails lastBill = null;
        boolean pelican = false;
        if (invoiceList != null && invoiceList.size() > 0) {
            currentBill = invoiceList.get(0);
            Double amount = currentBill.getAmount() / 100;
            BigDecimal bd = new BigDecimal(amount);
                bd = bd.setScale(2, RoundingMode.HALF_UP);
            response = bd.toString();
            
            lastBill = invoiceList.get(1);
            
            if (lastBill.getAmount() < currentBill.getAmount())
                pelican = true;
            
            response += ":" + pelican;
                
        }
        
        return response;
    }
        
      @GET
      @Path("due/{accountID}/{billingProfileId}")
      public String getDue(@PathParam("accountID") String accountID, @PathParam("billingProfileId") String billingProfileId) {

      String response = "";
      GILRequestData giplRequestData = new GILRequestData();
      giplRequestData.setBillingProfID("billingprofile-puneet");
      giplRequestData.setAccountID("accountId-puneet");
      giplRequestData.setMaxItems(100);
      giplRequestData.setResponseCode("FR");


      List<InvoiceDetails> invoiceList = null;

      try {
          invoiceList = GetInvoiceListAPI.getinvoiceList(giplRequestData);
      } catch (Exception e) {
          
          e.printStackTrace();
      } 
      
      
      InvoiceDetails currentBill = null;
      InvoiceDetails lastBill = null;
      boolean pelican = false;
      if (invoiceList != null && invoiceList.size() > 0) {
          currentBill = invoiceList.get(0);
         
          
          response = new SimpleDateFormat("dd MMMM yyyy").format(currentBill.getDueDate());
          
      }
      
      return response;
      }    
        
  }
