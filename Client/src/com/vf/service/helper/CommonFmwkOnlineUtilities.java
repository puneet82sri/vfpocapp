package com.vf.service.helper;


import com.vodafone.online.eserv.constants.EServConstants;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapBusinessException;
import com.vodafone.online.eserv.framework.utility.common.ResourceLoaderUtility;
import com.vodafone.online.eserv.framework.utility.cookie.IOnlineUtilities;
import com.vodafone.online.eserv.framework.utility.decrypt.HmacHelper;
import com.vodafone.online.eserv.framework.utility.decrypt.exceptions.HmacDecryptionException;
import com.vodafone.online.eserv.framework.utility.encrypt.EncryptionUtility;
import com.vodafone.online.eserv.framework.utility.encrypt.HexUtility;
import com.vodafone.online.eserv.framework.utility.encrypt.constants.EncryptionConstants;
import com.vodafone.online.eserv.framework.utility.logger.VodafoneLogger;
import com.vodafone.online.eserv.framework.utility.resource.exception.ResourceLoadException;
import com.vodafone.online.eserv.framework.utility.resource.impl.ResourceLoaderSingleton;
import com.vodafone.online.eserv.managedbeans.sessioncontext.SessionContext;

import java.io.UnsupportedEncodingException;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.UnifiedJEXL;
import org.apache.commons.lang.RandomStringUtils;


/**
 * This class implements IOnlineUtilities.
 * @author: HPE.
 * @date: 02/05/2016.
 */
public class CommonFmwkOnlineUtilities implements IOnlineUtilities {
    /**
     * Holds httpResponse.
     */
    private HttpServletResponse httpResponse;
    /**
     * Holds request.
     */
    private HttpServletRequest request;
    /**
     * holds value for 1024.
     */
    private final int oneZeroTwoFour = 1024;
    /**
     * Constructor.
     */
    public CommonFmwkOnlineUtilities() {
        super();
    }
    /**
     * Constructor.
     * @param httpRequest HttpServletRequest.
     * @param response HttpServletResponse.
     */
    public CommonFmwkOnlineUtilities(final HttpServletRequest httpRequest, final HttpServletResponse response) {
            super();
            request = httpRequest;
            httpResponse = response;
    }
    /**
     * holds cookieMaxAge.
     */
    private static int cookieMaxAge = 0;
    /**
     * holds TWENTYFOUR.
     */
    private static final int TWENTYFOUR = 24;

    /**
     * holds SIXTY.
     */
    private static final int SIXTY = 60;
    /**
     * This method is used to create cookie. Cookie informations like name, max age etc are read from data file
     * passed as parameter and cookie value is encrypted using AES algorithm.
     * @param cookieValues String - This field contains value of the cookie.
     * @param dataFileName String - This field contains data file name which holds information related to cookie.
     *
     */
    public final void createCookie(final String cookieValues,
                    final String dataFileName) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Start of createCookie method");
            Map<String, String> cookieInfoMap = readCookieInfo(dataFileName);
            String valueForCookie = null;
            Cookie cookie = null;
            try {
                    valueForCookie = prepareCookieValues(cookieValues);
                    valueForCookie = HexUtility.stringToHex(valueForCookie);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie value in hex:: " + valueForCookie);

                    if (cookieInfoMap != null) {
                            if (cookieInfoMap.containsKey(EServConstants.COOKIE_NAME)) {
                                    cookie = new Cookie(cookieInfoMap.get(EServConstants.COOKIE_NAME),
                                                    valueForCookie);
                                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie Name: "
                                                                                       + cookie.getName());
                                    if (cookieInfoMap
                                                    .containsKey(EServConstants.COOKIE_DOMAIN_NAME)) {
                                            cookie.setDomain(cookieInfoMap
                                                            .get(EServConstants.COOKIE_DOMAIN_NAME));
                                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , " Cookie Domain: "
                                                                                               + cookie.getDomain());
                                    }
                                    if (cookieInfoMap
                                                    .containsKey(EncryptionConstants.DESKTOP_COOKIE_MAX_AGE)) {
                                            cookie.setMaxAge(Integer.parseInt(cookieInfoMap
                                                            .get(EncryptionConstants.DESKTOP_COOKIE_MAX_AGE))
                                                            * TWENTYFOUR * SIXTY * SIXTY);
                                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , " Cookie MaxAge: "
                                                                                               + cookie.getMaxAge());
                                    }
                                    if (cookieInfoMap.containsKey(EServConstants.COOKIE_PATH)) {
                                            cookie.setPath(cookieInfoMap
                                                            .get(EServConstants.COOKIE_PATH));
                                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , " Cookie Path: "
                                                                                               + cookie.getPath());
                                    }
                                    if (cookieInfoMap.containsKey(EServConstants.COOKIE_SECURE)) {
                                            cookie.setSecure(Boolean.parseBoolean(cookieInfoMap
                                                            .get(EServConstants.COOKIE_SECURE)));
                                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , " Cookie isSecure: "
                                                                                               + cookie.getSecure());
                                    }
                            }
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "HTTPServlet Response.." + httpResponse);
                            httpResponse.addCookie(cookie);
                            request.setAttribute("COOKIEVALUE", valueForCookie);
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie added to response: "
                                                                               + httpResponse
                                                                               + "; and to request: "
                                                                               + request);
                    } else {
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "cookieInfoMap is null");
                    }
            } catch (Exception e) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Wrap BusinessException Occured...."
                                                                       + e.getMessage());
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "End of createCookie method");
    }

    /**
     * This method is used to delete the cookie. Cookie value is nullified.
     *
     * @param cookieName String - This holds name of the cookie to be deleted.
     * @return boolean - This field indicates status of cookie deletion.
     */
    public final boolean deleteCookie(final String cookieName) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Before deleting cookie ::");
            String randomNumber = generateRandomNubmer();
            String hexVAlue = null;
            if (request != null) {
                    Cookie[] cookies = request.getCookies();
                    if (null != cookies && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase(cookieName)) {
                        try {
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                                               "Generated Random Number : " + randomNumber);
                            hexVAlue = HexUtility.stringToHex(randomNumber);
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Hexadecimal Random Number :" + hexVAlue);
                            cookie.setValue(hexVAlue);
                            cookie.setMaxAge(0);
                            cookie.setPath(EncryptionConstants.BACKSLASH_DELIMETER);
                            httpResponse.addCookie(cookie);
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Cookie Nullified and added to response");
                            return true;
                        } catch (UnsupportedEncodingException e) {
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                                               "Exception - Delete Cokkie ::" + e.getMessage());
                            return false;
                        }
                    }
                }
            }
        }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "After deleting cookie ::");
            return false;
    }

    /**
     * This method determines whether httpRequest contains any cookie with name passed as input parameter.
     * @param cookieName String - This field contains name of the cookie to be searched in httpRequest.
     * @return boolean -  This field is set to true if cookie is found and false otherwise.
     */
    public final boolean isCookieExists(final String cookieName) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Start of isCookieExists method");
            if (request != null) {
                    Cookie[] cookies = request.getCookies();
                    if (null != cookies && cookies.length > 0) {
                            for (Cookie cookie : cookies) {
                                    if (cookie.getName().equals(cookieName)) {
                                  VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie Found. It Exists!!");
                                            return true;
                                    }
                            }
                    }
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie Not Found");
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "End of isCookieExists method");
            return false;
    }

    /**
     * This method is used to get value of cookie..
     *
     * @param cookieName String - This field contains name of the cookie.
     * @return String - This holds the value of cookie if found in httprequest, null otherwise.
     */
    public final String getCookieValue(final String cookieName) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Start of getCookieValues method");
            String cookieValues = null;
            if (request != null) {
                    Cookie[] cookies = request.getCookies();
                    if (null != cookies && cookies.length > 0) {
                            for (Cookie cookie : cookies) {
                                    if (cookie.getName().equalsIgnoreCase(cookieName)) {
                                      VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie Found. It Exists !!");
                                            cookieValues = cookie.getValue();
                                    }
                            }
                    }
            }
            return cookieValues;
    }

    /**
     * This method returns the application HttpSession object.
     *
     * @return HttpSession
     */
    public final HttpSession getApplicationHTTPSession() {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Getting J2EE Session Object");
            return request.getSession();
    }

    /**
     * This method returns the application HttpRequest object.
     *
     * @return HttpServletRequest
     */
    public final HttpServletRequest getApplicationHTTPRequest() {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Getting J2EE Request Object");
            return request;
    }

    /**
     * This method returns the application HttpResponse object.
     *
     * @return HttpServletResponse
     */
    public final HttpServletResponse getApplicationHTTPResponse() {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Getting J2EE Response Object");
            return httpResponse;
    }

    /**
     * This method is used to read cookie information from the config file
     * asset.
     * @param dataFileName String - This field contains file name .
     * @return cookieInfoMap - This holds the content of data file if found, null otherwise.
     */
    public static Map<String, String> readCookieInfo(final String dataFileName) {
            Map<String, String> cookieInfoMap = null;
            if (null != dataFileName) {
                    try {
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Loading the file in"
                                                                      + "readFileForCookieMaxAge(): " + dataFileName);
                            cookieInfoMap = ResourceLoaderUtility
                                            .getConfigFile(dataFileName);
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "after getting the"
                                               + "taskFlowProperties in readFileForCookieMaxAge()" + cookieInfoMap);
                    } catch (ResourceLoadException res) {
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG ,
                                            "ResourceLoadException Occured while Loading the xml file ",
                                            res);
                    }
            } else {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Data File Name is Null");
            }
            return cookieInfoMap;
    }

    /**
     * This method is used to append additional information to cookie value before creation of cookie..
     *
     * @param valueToBeEncrypted String - This field holds the value of cookie that needs to be encrypted.
     * @return String - This holds encrypted value of cookie.
     * @throws WrapBusinessException
     *             wrapBusinessException
     */
    public static String prepareCookieValues(final String valueToBeEncrypted)
                    throws WrapBusinessException {
            StringBuilder cookieEncrypted = new StringBuilder();
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Username passed to prepareCookieValues "
                            + valueToBeEncrypted);
            cookieEncrypted.append(prepareExpDateValueForCookie());
            cookieEncrypted.append(EncryptionConstants.KEY_DELIMITER);
            cookieEncrypted.append(prepareP1ValueForCookie(valueToBeEncrypted));
            cookieEncrypted.append(EncryptionConstants.KEY_DELIMITER);
            cookieEncrypted.append(prepareP3ValueForCookie(cookieEncrypted
                            .toString()));
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Value for the Cookie to be prepared:: "
                            + cookieEncrypted.toString());
            return cookieEncrypted.toString();
    }

    /**
     * This method is to generate expiry date part of the cookie's value.
     *
     * @return String - This field holds max age information of cookie. This value is appened to actual cookie value
     *                   prior to cookie creation.
     */
    public static String prepareExpDateValueForCookie() {
            StringBuilder finalCookieVal = new StringBuilder();
            finalCookieVal.append(EncryptionConstants.KEY_FOR_EXPRY_DATE);
            Calendar todaysDatePlus30Days = Calendar.getInstance();
            // todaysDatePlus30Days.add(Calendar.DAY_OF_YEAR, THIRTY);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie Max Age in prepareExpDateValueForCookie():: "
                            + cookieMaxAge);
            todaysDatePlus30Days.add(Calendar.DAY_OF_YEAR, cookieMaxAge);
            SimpleDateFormat formatter = new SimpleDateFormat(
                            EncryptionConstants.DATE_FORMAT, Locale.UK);
            String dateValue = formatter.format(todaysDatePlus30Days.getTime());
            finalCookieVal.append(dateValue);
            return finalCookieVal.toString();
    }

    /**
     * This method is to generate P1 part of the cookie's value.
     *
     * @param valueToBeEncrypted String - This holds value of cookie to be encrypted.
     * @return String - This field holds cookie information.
     * @throws WrapBusinessException
     *             wrapBusinessException
     */

    public static String prepareP1ValueForCookie(final String valueToBeEncrypted)
                    throws WrapBusinessException {
            StringBuilder finalCookieVal = new StringBuilder();
            finalCookieVal.append(valueToBeEncrypted);
            finalCookieVal.append(EncryptionConstants.COMMA_DELIMETER);
            // The three parameters are separated by : which is a delimiter
            // "Expires" timestamp is set to a value of now + 30 days
            Calendar todaysDatePlus30Days = Calendar.getInstance();
            // todaysDatePlus30Days.add(Calendar.DAY_OF_YEAR, THIRTY);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie Max Age in prepareP1ValueForCookie():: "
                            + cookieMaxAge);
            todaysDatePlus30Days.add(Calendar.DAY_OF_YEAR, cookieMaxAge);
            SimpleDateFormat formatter = new SimpleDateFormat(
                            EncryptionConstants.DATE_FORMAT, Locale.UK);
            String dateValue = formatter.format(todaysDatePlus30Days.getTime());
            finalCookieVal.append(dateValue);
            // Getting the username from security context
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Value of P1 before encryption: "
                                                               + finalCookieVal.toString());
            String encryptedP1;
            try {
                    encryptedP1 = EncryptionUtility.encrypt(finalCookieVal.toString());
                    finalCookieVal = new StringBuilder();
                    String randomSalt = generateRandomNubmer();
                    finalCookieVal.append(EncryptionConstants.KEY_FOR_P1);
                    finalCookieVal.append(encryptedP1);
                    finalCookieVal.append(randomSalt);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Value of P1 after encryption: "
                                    + finalCookieVal.toString());
            } catch (Exception e) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Encryption: Failed " + e.getMessage());
            }
            return finalCookieVal.toString();
    }

    /**
     * This method is to generate P3 part of the cookie's value.
     *
     * @param resEncrypted String - This field is holds cookie value to be encrypted.
     * @return String - This contains encrypted cookie value.
     */
    public static String prepareP3ValueForCookie(final String resEncrypted) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Value of P3 before :: " + resEncrypted);
            HmacHelper hmacHelper = new HmacHelper();
            String stringBuilder;
            String hashP3Value = "";
            ResourceBundle propertyRB = null;
            try {
                    propertyRB = (ResourceBundle) ResourceLoaderSingleton.getInstance()
                                    .getResource(2, EncryptionConstants.LOCAL_LOCATION,
                                                    EncryptionConstants.ENVIRONMENT_FILE);
            } catch (ResourceLoadException e) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG ,
                                    "Inside Resource loader Exception Block of PUT method...", e);
            }
            if (null != propertyRB) {
                    String keyString = propertyRB
                                    .getString(EncryptionConstants.DEC_KEY_SOFT_LOGIN);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Value for key for decryption::." + keyString);
                    byte[] keyValue = new byte[keyString.length()];
                    char charToByte = ' ';
                    for (int index = 0; index < keyString.length(); index++) {
                            charToByte = keyString.charAt(index);
                            keyValue[index] = (byte) charToByte;
                    }
                    try {
                            hashP3Value = hmacHelper.createHMAC(new String(keyValue,
                                            EncryptionConstants.ENCODING_UTF), resEncrypted);
                    } catch (HmacDecryptionException hmacException) {
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Exception occured during HMAC encryption"
                                            + hmacException.getMessage());
                    } catch (UnsupportedEncodingException unsupExc) {
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Unsupported Encoding Exception :: ::"
                                            + unsupExc.getMessage());
                    }
            }
            stringBuilder = EncryptionConstants.KEY_FOR_P2 + hashP3Value;
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Value of P3 after :: " + stringBuilder);
            return stringBuilder;
    }

    /**
     * This method generates random number using string util class.
     * @return String - This holds generated random number.
     */
    public static String generateRandomNubmer() {
            return RandomStringUtils.randomAscii(EncryptionConstants.RANDOM_SALT);
    }

    /**
     * This method is used to resolve expression for jexl.
     *
     * @param expression String - This holds the expression to be resolved.
     * @return Object
     */
    public final Object resolveExpression(final String expression) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , " inside resolveExpression");
            JexlEngine jexl = new JexlEngine();
            jexl.setCache(oneZeroTwoFour);
            jexl.setLenient(true);
            jexl.setSilent(true);
            jexl.setStrict(false);
            jexl.setDebug(true);
            UnifiedJEXL uniJ = new UnifiedJEXL(jexl);
            org.apache.commons.jexl2.UnifiedJEXL.Expression exp = uniJ
                            .parse(expression);
            MapContext mapContext = new MapContext();
            mapContext.set("sessioncontext", new SessionContext());
            if (null == request.getSession().getAttribute("sessioncontext")) {
                    request.getSession().setAttribute("sessioncontext",
                                    exp.evaluate(mapContext));
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "resolveExpression:: "
                            + request.getSession().getAttribute("sessioncontext"));
            return request.getSession().getAttribute("sessioncontext");

    }

    /**
     * This method is used to store input object in http session with input key.
     *
     * @param sessionkey String - This holds key with which object will be stored into httpRequest.
     * @param value - This holds actual object that needs to be stored in httpRequest.
     */
    @Override
    public final void storeOnHTTPSession(final String sessionkey, final Object value) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Storing SessionContext Object in Session with key:"
                                                               + sessionkey + " and value:" + value);
            request.getSession().setAttribute(sessionkey, value);
    }

    /**
     * This method is used to create the value createPreferenceCookie cookie.
     * when user logs in to the application.
     * @param uniqueID String - This holds value that needs to be encrupted
     * @return String - This holds encrypted value of input if encryption is success, empty string otherwise.
     * @throws WrapBusinessException
     *             wrapBusinessException
     */
    public static String encryptPreferenceCookieValue(final String uniqueID)
                    throws WrapBusinessException {
            StringBuilder cookieValue = new StringBuilder();
            String cookieValueInString = "";
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Start of createPreferenceCookie : " + uniqueID);
            try {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie value before encryption::" + uniqueID);
                    cookieValue.append(EncryptionUtility.encrypt(uniqueID));
                    cookieValueInString = cookieValue.toString();
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie value after encryption::"
                                                                       + cookieValueInString);
                    cookieValueInString = HexUtility.stringToHex(cookieValueInString);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Cookie value after converting to Hex Value::"
                                    + cookieValueInString);
            } catch (UnsupportedEncodingException unsup) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Exception while converting to hex value :: "
                                    + unsup.getMessage());
                    throw new WrapBusinessException(
                                    "WrapBusinessexception while converting to hex value: ",
                                    null, null, unsup);
            }
            return cookieValueInString;
    }
/**
     * This method is used to get user name from userPrincipal object of httprequest.
     * If user name is found, then httpResponse is set as unAuthorised.
     * @return String - This holds the user name ontained from request if found, null otherwise.
     */
    @Override
    public final String getUserName() {
           VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "inside getUserName method");
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "UserPrincipal from UserPriciple:: "
                            + request.getUserPrincipal());
            String user = null;
//            if (getApplicationHTTPRequest().getUserPrincipal() != null) {
//                    user = getApplicationHTTPRequest().getUserPrincipal().getName();
//                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "UserPrincipal from SSO Request:: " + user);
//            } else {
//                try {
//                 httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UnAuthorised Request");
//                } catch (Exception e) {
//                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Error : " + e.getMessage());
//                }
//            }
            /** un comment Below Code , working in Local System - start **/
//            if (user == null) {
//                    user = "vodafoneuser";
//            }
            /** un comment, working in Local System - End **/
            user = "PuneetSrivastava";
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "User is :" + user);
            return user;
    }
/**
     *
     * @return boolean.
     */
    @Override
    public final boolean isAuthenticated() {
    //              boolean auth = false;
    //              Subject obj = weblogic.security.Security.getCurrentSubject();
    //              for (Principal p : obj.getPrincipals()) {
    //                      if (p.getName() != null) {
    //          VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Principal value from Subject Object" + p.getName());
    //                              auth = true;
    //                              break;
    //                      }
    //              }
    //              /** un comment Below Code , Working in Local System - start **/
    //              if (auth == false) {
    //                      auth = true;
    //              }
    //              /** un comment Below Code , working in Local System - End **/
    //              VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG , "Authenticated value is:: " + auth);
    //              return auth;
        return true;
    }

    // NC56 - Changes starts
    /**
     * The method is to remove the cookie from session. Currently default
     * implementation provided.
     *
     * @param cookieName String - name of the cookie
     * @param dataFileName String - cookie configuration file name
     * @return delete success or fail.
     */
    public final boolean deleteCookie(final String cookieName,
                    final String dataFileName) {
            return true;
    }
/**
     * Implemented from IOnlineUtilities interface by default. No fuctionality exists.
     * @param string String.
     * @param string1 String.
     */
    public void generateAnalyticTags(final String string, final String string1) {
        // Do nothing.  Exists to satisfy IOnlineUtilities interface.
    }
/**
     * Implemented from IOnlineUtilities interface by default. No fuctionality exists.
     * @param string String.
     * @return Object.
     */
    public final Object getFromApplicationSession(final String string) {
        return null;
    }
/**
     * Implemented from IOnlineUtilities interface by default. No fuctionality exists.
     * @param string String.
     * @param object Object.
     */
    public final void setExpressionValue(final String string, final Object object) {
        // Do nothing.  Exists to satisfy IOnlineUtilities interface.
    }
/**
     * Implemented from IOnlineUtilities interface by default. No fuctionality exists.
     * @param string String.
     * @param strings String[]
     * @param ints int[]
     */
    public void generateAnalyticTagsEnc(final String string, final String[] strings, final int[] ints) {
        // Do nothing.  Exists to satisfy IOnlineUtilities interface.
    }
/**
     * Setter method for httpResponse.
     * @param httpResponse HttpServletResponse.
     */
    public final void setHttpResponse(final HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
    }
/**
     * Getter method for httpResponse.
     * @param request HttpServletRequest.
     */
    public final void setRequest(final HttpServletRequest request) {
        this.request = request;
    }
}
