package com.vf.rest.service;

import com.vodafone.online.eserv.api.dynamiclanding.GetBalanceAPI;
import com.vodafone.online.eserv.framework.coherence.client.ProductSchemaCacheClient;
import com.vodafone.online.eserv.framework.jtc.dto.BaseVodafoneDTO;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapBusinessException;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapSystemException;
import com.vodafone.online.eserv.framework.jtc.factory.ProxyServiceFactory;
import com.vodafone.online.eserv.framework.utility.exception.CoherencePushException;
import com.vodafone.online.eserv.framework.utility.logger.VodafoneLogger;
import com.vodafone.online.eserv.services.request.getbalance.GetBalanceRequestDTO;

import com.vodafone.online.eserv.services.response.getbalance.GetBalanceResponseDTO;

import java.math.BigDecimal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@Path("/balance")
public class GetBalance {
    public GetBalance() {
        super();
    }
    
    
        @GET
        @Path("/{param}")
    public String getBalanceToString(@PathParam("param") String ctn) {   
            
//        GetBalanceRequestDTO dto = GetBalanceAPI.getBalanceRequestAPI("", "", "");
        
       GetBalanceRequestDTO getBalanceRequestDTO = getBalance(ctn, "billingProfileId", "assetIntegrationId","subscriptionType");
//        // START CR 649 | WCPR2 - ends
         GetBalanceResponseDTO getBalanceResponseDTO = null;
//
//
        try {
            getBalanceResponseDTO = getBalance(getBalanceRequestDTO);
        } catch (WrapSystemException e) {
            
            e.printStackTrace();
        } catch (WrapBusinessException e) {
            e.printStackTrace();
        }

        BigDecimal cost = getBalanceResponseDTO.getQueryCustomerPartyResponseDTO().getQueryCustomerPartyListResponseDTO().getCustomerPartyBillingProfileDTOList().get(0).getBalanceGroupDTOList().get(0).getResourceUnitDTOList().get(0).getResourceUnitBalanceDTO().getAvailableQuantity() ;
        return "Balance of " + ctn + " is " + cost.divide(new BigDecimal(100));
    }

    private GetBalanceRequestDTO getBalance(String msisdn, String billingProfileId,
                                            String assetIntegrationId, String subscriptionType) {

        // START CR 649 | WCPR2 - ends
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                           "Start of getBalance() - msisdn:" + msisdn);
        // START - Code change  for Production issue - PAYM user | Login delay issue
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                            "Billing Prof Id :: :" + billingProfileId);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                            "Asset Integration id:: :" + assetIntegrationId);
        // END - Code change  for Production issue - PAYM user | Login delay issue
        GetBalanceRequestDTO getBalanceRequestDTO = new GetBalanceRequestDTO();
        // START CR 649 | WCPR2
//        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "subscriptionType ==>" + subscriptionType);
//        if (null != subscriptionType && (subscriptionType.equalsIgnoreCase(BalanceCheckerConstants.FIXED.getValue())
//        || subscriptionType.equalsIgnoreCase(BalanceCheckerConstants.FIXEDLINE.getValue()))) {
//            getBalanceRequestDTO.setElementPath(TilServicesConstants.TilServiceParams.FLN.name());
//        } else {
//            getBalanceRequestDTO.setElementPath(TilServicesConstants.TilServiceParams.MSISDN.name());
//        }
        getBalanceRequestDTO.setElementPath("MSISDN");
        // START CR 649 | WCPR2 - ends
        getBalanceRequestDTO.setValue(msisdn);
        /*
        // R2.3 Defect fix | 38263 changed from FR to FR_Usage| Start
        getBalanceRequestDTO.setResponseCode(TilServicesConstants.TilServiceParams.FR_Usage.name());
        // R2.3 Defect fix | 38263 changed from FR to FR_Usage| End
        */
        //START|R2.3|40653|FR to FR_Stacked
        getBalanceRequestDTO.setResponseCode("FR");
        //END|R2.3|40653|FR to FR_Stacked
        getBalanceRequestDTO.setSendSMS(true);
        getBalanceRequestDTO.setServiceName("GetBalance");
        // START - Code change  for Production issue - PAYM user | Login delay issue
        getBalanceRequestDTO.setAssetIntegrationId(assetIntegrationId);
        getBalanceRequestDTO.setBillingProfileId(billingProfileId);
        // END - Code change  for Production issue - PAYM user | Login delay issue
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                           "End of getBalance()");
        return getBalanceRequestDTO;
        
    }

    private GetBalanceResponseDTO getBalance(GetBalanceRequestDTO getBalanceRequestDTO) throws WrapSystemException,
                                                                                               WrapBusinessException {
        GetBalanceResponseDTO getBalanceResponseDTO = null;
        BaseVodafoneDTO serviceResponse = null;
        serviceResponse =
                  ProxyServiceFactory.invokeService(getBalanceRequestDTO);
          getBalanceResponseDTO = (GetBalanceResponseDTO) serviceResponse;
          VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                             "getBalanceResponseDTO has been created-->"
                             + getBalanceResponseDTO.toString());
          VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "End of getBalance()");
         return getBalanceResponseDTO;
    }
}
