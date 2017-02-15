package com.vf.rest.service;


import com.vodafone.online.eserv.framework.jtc.exceptions.WrapBusinessException;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapSystemException;
import com.vodafone.online.eserv.framework.utility.cookie.IOnlineUtilities;
import com.vodafone.online.eserv.framework.utility.exception.SIException;
import com.vodafone.online.eserv.framework.utility.logger.VodafoneLogger;
import com.vodafone.online.eserv.helper.SessionInitializationhelper;
import com.vodafone.online.eserv.managedbeans.sessioncontext.AuthNStatus;
import com.vodafone.online.eserv.managedbeans.sessioncontext.SessionContext;

import com.vodafone.online.eserv.services.constants.ServiceConstants;
import com.vf.service.helper.CommonFmwkOnlineUtilities;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;


/**
 * This is service implemented for building session. It accepts account ID and subscription ID. If passed account ID and
 * subscription ID are valid, session context is updated with account ID and subscription ID passed through request and
 * sessionContext object is pushed into coherence.
 * @author: HPE.
 * @date: <02/05/2016>
 */
@Path("/session")
public class SessionContextService {
    /**
     * Constructor.
     */
    public SessionContextService() {
        super();
    }
    /**
     *This is service for building session. This service gets username from userPrincipal object of httprequest and
     * invokes session API to return SessionContext object. If account ID and subscription ID passed are valid,
     * sessionContext object is updated accordingly and pushed into coherence.
     * This webservice returns ServiceResponse object.
     * If validation of account id or subscription id fails, then status of response is set to WARNING and appropriate
     * message is populated in message field.
     * In case of any failures/Exception while building session/validation status of response is set to FAILURE and
     * appropriate message is populated in message field.
     * @param acctId String - This holds account ID passed through URL.
     * @param subscriptionID String - This holds subscription ID passed through URL.
     * @param httpRequest HttpServletRequest
     * @param httpResponse HttpServletResponse
     * @return ServiceResponse - This object holds webservice response indicating status of status of session
     *                           building/updation along with appropriate message/session key.
     */
    @Path("/context/v1/{acctID}/{subsID}")
    @GET
    
    public final String updateSessionContext(@PathParam("acctID") final String acctId,
                                                             @PathParam("subsID")
        final String subscriptionID, @Context final HttpServletRequest httpRequest,
        @Context final HttpServletResponse httpResponse) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Start of updateSessionContext with parameters : "
                                                         + "Account ID : "
                                                         + acctId
                                                         + " subscription ID : "
                                                         + subscriptionID);
        long startTime = 0;
        long endTime = 0;
        startTime = System.currentTimeMillis();
//        SimpleDateFormat sdf = new SimpleDateFormat(ServiceConstants.TIME_FORMAT, Locale.UK);
//        String timeStart = sdf.format(Calendar.getInstance().getTime());
//        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, ": CURRENT_MILLISECONDS : " + timeStart);
        String vfSessionKey = null;
        String encryptedKey = null;
        SessionContext sessionContext = null;
        //initializing CommonFmwkOnlineUtilities,Here all the cookie related methods are available.
        
        
        IOnlineUtilities iol = new CommonFmwkOnlineUtilities(httpRequest, httpResponse);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Creating IOnlineUtilities instance");
            try {
                if (null != iol.getUserName()) {
                    sessionContext = SessionInitializationhelper.buildSession(iol);
                    
                }
            }  catch (Exception e) {
//                response = new SessionServiceResponse(encryptedKey, MessageType.FAILURE.getValue(),
//                                                      ServiceConstants.ERR_MSG_GENERIC_EXCEPTION);
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_ERROR, "Exception Occurred" + e.getMessage());
            }
//        if (null != response) {
//            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "End of updateSessionContext with response : "
//                                                             + response);
//        }
//        response =new SessionServiceResponse("Hello Puneet", MessageType.SUCCESS.getValue(),
//                                                      ServiceConstants.SESSION_DETAILS); 
        
        
//        endTime = System.currentTimeMillis();
//        String timeEnd = sdf.format(Calendar.getInstance().getTime());
//        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "CURRENT_MILLISECONDS : " + timeEnd);
//        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "TOTAL_TIME_TAKEN_MILLISECONDS: " + (endTime - startTime));
//        response.
        return "Session building complete";
    }
}