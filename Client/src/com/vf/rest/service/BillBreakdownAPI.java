package com.vf.rest.service;


import com.vodafone.online.eserv.component.productschema.ProductSchemaService;
import com.vodafone.online.eserv.constants.BaseConstants;
import com.vodafone.online.eserv.constants.BillBreakdownAPIConstants;
import com.vodafone.online.eserv.ebillingv2.AccountDetails;
import com.vodafone.online.eserv.ebillingv2.BundleDetails;
import com.vodafone.online.eserv.ebillingv2.DetailsSubscriptionUsage;
import com.vodafone.online.eserv.ebillingv2.InvoiceAccountCharge;
import com.vodafone.online.eserv.ebillingv2.InvoiceSummarySubscriptions;
import com.vodafone.online.eserv.ebillingv2.InvoiceType;
import com.vodafone.online.eserv.ebillingv2.UsageDetails;
import com.vodafone.online.eserv.exceptions.ProductSchemaException;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapBusinessException;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapSystemException;
import com.vodafone.online.eserv.framework.jtc.factory.ProxyServiceFactory;
import com.vodafone.online.eserv.framework.utility.cookie.IOnlineUtilities;
import com.vodafone.online.eserv.framework.utility.logger.VodafoneLogger;
import com.vodafone.online.eserv.framework.utility.resource.constants.ResourceConstants;
import com.vodafone.online.eserv.framework.utility.resource.exception.ResourceLoadException;
import com.vodafone.online.eserv.framework.utility.resource.impl.ResourceLoaderSingleton;
import com.vodafone.online.eserv.framework.utility.xmlparser.XMLMessageProcessor;
import com.vodafone.online.eserv.framework.utility.xmlparser.XMLMessageProcessorInterface;
import com.vodafone.online.eserv.framework.utility.xmlparser.exception.VFXmlException;
import com.vodafone.online.eserv.helper.GetProductListHelper;
import com.vodafone.online.eserv.helper.ebillingv2.BillBreakdownAPIHelper;
import com.vodafone.online.eserv.managedbeans.sessioncontext.SessionContext;
import com.vodafone.online.eserv.managedbeans.sessioncontext.Subscription;
import com.vodafone.online.eserv.managers.SessionInitializationManager;
import com.vodafone.online.eserv.productcatalog.Bundle;
import com.vodafone.online.eserv.productcatalog.Product;
import com.vodafone.online.eserv.schemas.datafile.ElementType;
import com.vodafone.online.eserv.schemas.datafile.ListRowType;
import com.vodafone.online.eserv.schemas.datafile.ListType;
import com.vodafone.online.eserv.schemas.datafile.RootDocument;
import com.vodafone.online.eserv.services.request.getinstalledproductslist.GetInstalledProductListRequestDTO;
import com.vodafone.online.eserv.services.request.getinvoicedetails.GetInvoiceDetailsRequestDTO;
import com.vodafone.online.eserv.services.request.getinvoicesummary.GetInvoiceSummaryRequestDTO;
import com.vodafone.online.eserv.services.response.getinstalledproductslist.GetInstalledProductListResponseDTO;
import com.vodafone.online.eserv.services.response.getinvoicedetails.GetInvoiceDetailsResponseDTO;
import com.vodafone.online.eserv.services.response.getinvoicedetails.QueryInvoiceListDTO;
import com.vodafone.online.eserv.services.response.getinvoicesummary.GetInvoiceSummaryResponseDTO;
import com.vodafone.online.eserv.services.response.getinvoicesummary.InvoiceLineDTO;
import com.vodafone.online.eserv.sessioncontextapi.SessionContextSubscriptionAPI;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.math.BigDecimal;

import java.nio.charset.Charset;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;

import org.apache.xmlbeans.XmlObject;

//import org.powermock.api.mockito.PowerMockito;

//import javax.sound.sampled.Line;


//import org.apache.commons.lang.StringUtils;


/**
 * This is an API class for calling the GIS & GID til and then populating the response
 * that we get from the TIL into the Data Model available for Ebilling and returning it
 * back to the Bill Breakdown Taskflow.
 */
public final class BillBreakdownAPI {
    /**
     * Default Construtor.
     */
    private BillBreakdownAPI() {
        super();
    }
    /**
     * This is the API that will be called from the BillBreakdown taskflow from the default activity which
     * will call the GetInvoiceSummary Til by passing the serviceAccountID, BillingProfileID and the invoiceID
     * coming from the PAY02 or PAY04 as request and then populating the Response for each of the subscription
     * present in the session and then the API will sort the Subscription based on the configured value in config
     * file.This API will aslo call the GetInvoiceDetails TIL for getting the bundleDescription and the Discount
     * description and also the account charge information.
     * @param serviceAccountID incontext account id to call GIS til.
     * @param invoiceID invoice ID coming from either PAY02 or Pay04.
     * @param billingProfileID incontext billing profile id coming from session.
     * @param configMapListType taskflow congiguration map used for sorting the subscription and geting the
     * typecode information.
     * @param iOnlineUtilities var-arg for which will be used to build session when called from Junit.
     * @param configMap Map of String and string for getting the configured value of type code used for
     * account charges.
     * @return AccountDetails object which will contain the List of accountcharge object and Map of
     * subscription ID as the key and InvoiceSummarySubscriptions object as the value.
     * @throws WrapSystemException
     * @throws WrapBusinessException
     */
    public static final AccountDetails fetchBillSymmaryAndAccDetails(final String serviceAccountID,
                                                final String invoiceID, final String billingProfileID,
                                                final Map<String, List> configMapListType,
                                                final Map<String, String> configMap,
                                                final IOnlineUtilities... iOnlineUtilities)
                                                throws WrapSystemException, WrapBusinessException,
                                                ProductSchemaException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of fetchBillSymmaryAndAccDetails method");
        AccountDetails accountDetails = null;
        GetInvoiceSummaryRequestDTO invoiceSummaryRequest = null;
        GetInvoiceSummaryResponseDTO invoiceSummaryResponse = null;
        Map<String, Subscription> filteredSubscription = null;
        Map<String, InvoiceSummarySubscriptions> invoiceSummaryMap = null;
        List<InvoiceType> invoiceTypeList = null;
        //Preparing the request for calling the GetInvoiceSummary Til.
        invoiceSummaryRequest = BillBreakdownAPIHelper.prepareRequestForGetInvoiceSummary(serviceAccountID,
                                                                                    invoiceID, billingProfileID);
        //Calling the GetInvoiceSummary TIL by passing the request generated in previous step.
        if (null != invoiceSummaryRequest) {
        invoiceSummaryResponse = getInvoiceSummary(invoiceSummaryRequest);
        }
        if (null != invoiceSummaryResponse) {
            //Get the invoiceTypeList for type code cycle-forward/Adjustment/Purchase fee/Refund
            //Retrieving the subscritionList that will be displayed on Pay08 taskflow by taking only those subscription
            //present in both session and GIS til.
            filteredSubscription = retrieveApplicableSubscriptionList(invoiceSummaryResponse,
                                                                      serviceAccountID, iOnlineUtilities);
            // CR || CI11 || 51407 || Start
            invoiceTypeList = getInvoiceTypeList(invoiceSummaryResponse, configMapListType);
            // CR || CI11 || 51407 || End
            if (null != filteredSubscription) {
                //Populating the GetInvoiceSummary response
                invoiceSummaryMap = prepareInvoiceSummary(filteredSubscription,
                                            invoiceSummaryResponse, configMapListType, configMap);
                accountDetails = new AccountDetails();
                accountDetails.setCurrentInvoiceSubscriptionSummary(invoiceSummaryMap);
                //Getting the Account charge information from GIS and GID til by passing the GIS response and
                //billingAccountID.
                //accountCharges = retrieveInvoiceAccountCharge(configMap, invoiceSummaryResponse, serviceAccountID);
                //accountDetails.setInvoiceAccountCharges(accountCharges);
                accountDetails.setInvoiceTypeList(invoiceTypeList);
            }
        }
        if (null != accountDetails) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "AccountDetails in fetchBillSymmaryAndAccDetails"
                                                + accountDetails.toString());
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of fetchBillSymmaryAndAccDetails method");
        return accountDetails;
    }
    /**
     * This method is used to call the GetInvoiceSummary Til call and send back the Response that is
     * received from the TIl.
     * @param invoiceSummaryRequest request containing the accountID,BillingprofileID and invoiceID.
     * @return GetInvoiceSummaryResponseDTO received from the GIS Til.
     * @throws WrapSystemException
     * @throws WrapBusinessException
     */
    private static GetInvoiceSummaryResponseDTO getInvoiceSummary(final GetInvoiceSummaryRequestDTO
                                        invoiceSummaryRequest) throws WrapSystemException, WrapBusinessException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of getInvoiceSummary method");
        GetInvoiceSummaryResponseDTO invoiceSummaryResponse = null;
        if (null != invoiceSummaryRequest) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                               "requestDTO is not null");
            invoiceSummaryResponse = (GetInvoiceSummaryResponseDTO) ProxyServiceFactory
                                                                .invokeService(invoiceSummaryRequest);
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of getInvoiceSummary method");
        return invoiceSummaryResponse;
    }
    /**
     * This method is used to get the List of all the Subscription that are present in both
     * under GIS til and all the subscription List present user the active account.
     * @param invoiceSummaryResponse Response Received from the GIS til.
     * @param accountID serviceaccount id.
     * @param iOnlineUtilities used for session building when called from junit.
     * @return List of only those Subscription that are present in Session and GIS both.
     */
    private static Map<String, Subscription> retrieveApplicableSubscriptionList(final GetInvoiceSummaryResponseDTO
                                                invoiceSummaryResponse, final String accountID,
                                                final IOnlineUtilities... iOnlineUtilities)
                                                throws WrapSystemException, WrapBusinessException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of retrieveApplicableSubscriptionList method");
        List<Subscription> allSubscriptionList = null;
        Map<String, Subscription> filteredSubscriptionMap = null;
        Subscription subscription = null;
        //Code changes for WR33296 | Starts
        Map<String, Subscription> inactiveSubscriptionMap = null;
        String formattedSerialNumber = null;
        List<String> inactiveSubscriptions = null;
        //Code changes for WR33296 | Ends
        //Getting all the subscription under the active account from SessionContextSubscriptionAPI.
        allSubscriptionList = SessionContextSubscriptionAPI.getAllSubscription(iOnlineUtilities);
        //If the GIS response is not null and the subscriptionList is not null then comparing the subscription
        //present in session with the subscription coming from GIS Til and preparing the list of only those
        //Subscription that are Present in Session and discarding rest.
        if (null != allSubscriptionList && null != invoiceSummaryResponse
            && null != invoiceSummaryResponse.getQueryinvoicelistresponse()
                && null != invoiceSummaryResponse.getQueryinvoicelistresponse().getInvoiceline()) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "SubscriptionList from Session"
                                                              + allSubscriptionList.size());
            //Code changes for WR33296 | Starts
            filteredSubscriptionMap = new HashMap<String, Subscription>();
            inactiveSubscriptions = new ArrayList<String>();
            for (InvoiceLineDTO invoiceLine : invoiceSummaryResponse
                                    .getQueryinvoicelistresponse().getInvoiceline()) {
                if (null != invoiceLine && null != invoiceLine.getIdentificationDTO()
                    && null != invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO()
                    && null != invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId()
                    && !invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId().isEmpty()) {
                    if (checkForInactiveSubscription(invoiceLine.getIdentificationDTO()
                                                            .getAlternativeObjectKeyDTO().getId())) {
                        formattedSerialNumber = BillBreakdownAPIHelper.changeInactiveCTNFormat(
                                        invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId());
                        inactiveSubscriptions.add(formattedSerialNumber);
                    } else {
                        subscription = fetchSubscription(invoiceLine.getIdentificationDTO()
                                                .getAlternativeObjectKeyDTO().getId(), allSubscriptionList);
                        if (null != subscription) {
                            filteredSubscriptionMap.put(subscription.getSerialNumber(), subscription);
                        }
                    }
                }
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "SubscriptionMap after filtering"
                                                          + filteredSubscriptionMap.size());
            if (!inactiveSubscriptions.isEmpty() && accountID != null) {
                inactiveSubscriptionMap = retrieveInactiveSubscriptionFromGIPL(inactiveSubscriptions, accountID);
                if (null != inactiveSubscriptionMap && !inactiveSubscriptionMap.isEmpty()) {
                    filteredSubscriptionMap.putAll(inactiveSubscriptionMap);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                        "SubscriptionMap after adding inactive subscription" + filteredSubscriptionMap.size());
                }
            }
            //Code changes for WR33296 | Ends
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of retrieveApplicableSubscriptionList method");
        return filteredSubscriptionMap;
    }
    //Code changes for WR33296 | Starts
    /**
     * This method is used to call the GIPl for the inactive subscription and populate all the inactive subscription
     * from the GIPL response which will be used displaying purpose in Ebilling.
     * @param inactiveSubscription list of inactive subscription.
     * @param accountID serviceaccountid used for calling GIPL.
     * @return Map of inactive subscription.
     * @throws WrapSystemException
     * @throws WrapBusinessException
     */
    private static final Map<String, Subscription> retrieveInactiveSubscriptionFromGIPL(
                                                    final List<String> inactiveSubscription, final String accountID)
                                                    throws WrapSystemException, WrapBusinessException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of retrieveInactiveSubscriptionFromGIPL method");
        Map<String, Subscription> inactiveSubscriptionMap = null;
        GetInstalledProductListRequestDTO requestDTO = null;
        GetInstalledProductListResponseDTO responseDTO = null;
        if (null != accountID && null != inactiveSubscription) {
            requestDTO = GetProductListHelper.getInstalledProductRequestForInactiveSubscription(accountID);
            responseDTO = SessionInitializationManager.getInstalledProductList(requestDTO);
            inactiveSubscriptionMap = BillBreakdownAPIHelper
                        .populateGIPLResponseForInactiveSubs(inactiveSubscription, responseDTO);
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of retrieveInactiveSubscriptionFromGIPL method");
        return inactiveSubscriptionMap;
    }
    //Code changes for WR33296 | Ends
     /**
      * This method is used to populate the GIS response into the POJO object by taking the subscriptionList and
      * Response received from the GIS til and then populating the MAP with key as subscriptionID and value as the
      * InvoiceSummarySubscriptions.
      * @param subscriptionMap map of subscriptionID as the key and subscription object as the value.
      * @param invoiceSummaryResponse response received from GIS til
      * @param configMapListType taskflow config map for getting the sorting order of subscription and the type code
      * mapping.
      * @param configMap Map of string and string for getting the configured type code for Account charge scenario.
      * @return Map with key as Subscription ID and value as the InvoiceSummarySubscriptions for each of the
      * subscription present in session.
      */
     private static final Map<String, InvoiceSummarySubscriptions> prepareInvoiceSummary(
                                                   final Map<String, Subscription> subscriptionMap,
                                                   final GetInvoiceSummaryResponseDTO invoiceSummaryResponse,
                                                   final Map<String, List> configMapListType,
                                                   final Map<String, String> configMap)
                                                   throws ProductSchemaException {
         VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of prepareInvoiceSummary method");
         Map<String, InvoiceSummarySubscriptions> invoiceSummaryMap = null;
         InvoiceSummarySubscriptions invoiceSummary = null;
         InvoiceType bundleInvoiceType = null;
         String formattedSerialNumber;
         if (null != subscriptionMap & null != invoiceSummaryResponse
             && null != invoiceSummaryResponse.getQueryinvoicelistresponse()
                 && null != invoiceSummaryResponse.getQueryinvoicelistresponse().getInvoiceline()) {
             VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Inside condition");
                 //Calling the method to populate the subscription detail in InvoiceSummarySubscription POJO
                 // and the itemizeddetails list and extrausaga details list based on the config map.
                 invoiceSummaryMap = populateInvoiceSummarySubscription(subscriptionMap, configMapListType);
                 for (InvoiceLineDTO invoiceLine : invoiceSummaryResponse
                                 .getQueryinvoicelistresponse().getInvoiceline()) {
                    if (null != invoiceLine && null != invoiceLine.getIdentificationDTO()
                        && null != invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO()
                        && null != invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId()
                        && !invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId().isEmpty()) {
                        //Calling the method to remove the 44 from the start of the serialnumber present in Til.
                        formattedSerialNumber = BillBreakdownAPIHelper.changeCTNFormat(invoiceLine
                                                .getIdentificationDTO().getAlternativeObjectKeyDTO().getId());
                        //Defect fix 53490 | start
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Formatted serial number:: "
                                                                         + invoiceLine
                                                    .getIdentificationDTO().getAlternativeObjectKeyDTO().getId());
                            if (invoiceSummaryMap.containsKey(formattedSerialNumber)) {
                                invoiceSummary = invoiceSummaryMap.get(formattedSerialNumber);
                            } else if (invoiceSummaryMap.containsKey(invoiceLine
                                                    .getIdentificationDTO().getAlternativeObjectKeyDTO().getId())) {
                                invoiceSummary = invoiceSummaryMap.get(invoiceLine
                                                    .getIdentificationDTO().getAlternativeObjectKeyDTO().getId());
                            } else {
                                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "No match found");
                            }
                        if (null != invoiceSummary) {
                            //Defect fix 53490 | end
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Reference name is"
                                + invoiceSummary.getSubDescription() + "Bundle Description is"
                                + invoiceSummary.getSubBundleDescription() + "SubscriptionType is"
                                + invoiceSummary.getSubscriptionType());
                            //Checking the condition if the type is equal to Cycle-forward for bundle charge and
                            //discount.If the extended amount is positive then setting the bundle charge otherwise
                            //setting the discount charge and the description value from GID.
                            bundleInvoiceType = fetchBundleCharges(invoiceLine, configMap);
                            if (null != bundleInvoiceType) {
                                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Inside condition for cycle-forward");
                                invoiceSummary.setInvoiceType(bundleInvoiceType);
                                //Add logic for bundle charge
                                invoiceSummaryMap.put(invoiceSummary.getSubscriptionID(), invoiceSummary);
                                continue;
                            } else {
                                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Inside condition for othr typecode");
                                //Code Change for Defect 53375 | Starts
                                fetchUsageDetailList(invoiceSummary, invoiceLine, configMapListType);
                                //Code Change for Defect 53375 | Ends
                            }
                        invoiceSummaryMap.put(invoiceSummary.getSubscriptionID(), invoiceSummary);
                        }
                    }
                }
         }
         VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of prepareInvoiceSummary method");
         return invoiceSummaryMap;
     }
    //Code Change for Defect 53375 | Starts
    /**
     * This Method is For Creating UsageDetailList.
     * @param invoiceSummary InvoiceSummarySubscriptions.
     * @param invoiceLine InvoiceLineDTO.
     * @param configFile Map.
     */
    private static void fetchUsageDetailList(final InvoiceSummarySubscriptions invoiceSummary,
                                             final InvoiceLineDTO invoiceLine, final Map<String, List> configFile) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of fetchUsageDetailList method");
        List<UsageDetails> usageDetailsList = invoiceSummary.getExtraUsageDetails();
        BigDecimal extraUsage;
        Map<String, String> subscriptionChargeIDMap = null;
        String subscriptionChargeID = null;
        boolean typeCodeMatched = Boolean.FALSE;
        List<Map<String, String>> typeCodeList = null;
        for (UsageDetails usage : usageDetailsList) {
            if (null != usage &&  null != invoiceLine.getItemreference() && null != invoiceLine.getItemreference()
                .getTypecode()) {
                if (invoiceLine.getItemreference().getTypecode().equalsIgnoreCase(usage.getSubscriptionTypeCode())) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "inside condition of subscriptionTypecode");
                    typeCodeMatched = Boolean.TRUE;
                } else if (null != configFile && null != configFile.get(BillBreakdownAPIConstants.OTHER_TYPE_CODE)) {
                    typeCodeList = configFile.get(BillBreakdownAPIConstants.OTHER_TYPE_CODE);
                    for (Map<String, String> typeCodes : typeCodeList) {
                        if (invoiceLine.getItemreference().getTypecode()
                            .equalsIgnoreCase(typeCodes.get(BillBreakdownAPIConstants.TYPE_CODE))
                            && usage.getSubscriptionUsageType().equalsIgnoreCase(
                            typeCodes.get(BillBreakdownAPIConstants.TYPE))) {
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                                               "inside condition of Adjustment & purchasefee");
                            typeCodeMatched = Boolean.TRUE;
                        }
                    }
                }
                if (typeCodeMatched) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "typecode from til is"
                                                + invoiceLine.getItemreference().getTypecode());
                    subscriptionChargeID = fetchItemChargeID(invoiceLine);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "itemchargeid is"
                                                                          + usage.getSubscriptionChargeID());
                    if (null != usage.getSubscriptionChargeID()) {
                        usage.getSubscriptionChargeID().put(invoiceLine.getItemreference().getTypecode(),
                                                            subscriptionChargeID);
                    } else {
                        subscriptionChargeIDMap = new HashMap<String, String>();
                        subscriptionChargeIDMap.put(invoiceLine.getItemreference().getTypecode(),
                                                    subscriptionChargeID);
                        usage.setSubscriptionChargeID(subscriptionChargeIDMap);
                    }
                        //usage.setSubscriptionChargeID(fetchItemChargeID(invoiceLine));
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "extrausage charge before adding is"
                                                                      + usage.getExtraUsageCharge());
                    if (null != invoiceLine.getExtendedAmount()) {
                        extraUsage = BillBreakdownAPIHelper.convertToTwoDecPlaces(
                                        BillBreakdownAPIHelper.penceToPoundConversion(invoiceLine.getExtendedAmount().
                                                                toString(), BillBreakdownAPIConstants.HUNDRED));
                        usage.setExtraUsageCharge(usage.getExtraUsageCharge().add(extraUsage));
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "extrausage charge after adding is"
                                                                          + usage.getExtraUsageCharge());
                    }
                    break; //NOPMD
                }
            }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of fetchUsageDetailList method");
    }
    //Code Change for Defect 53375 | Ends
     /**
     * This method is used to fetch the bundle description by calling the getCommercialBundle API which
     * internally calls the coherence and get the bundleBundle description.
     * @param bundleID bundleID for the subscription.
     * @return the bundle Description that it gets from coherence.
     */
     private static final String fetchBundleDescriptionFromCoherence(final String bundleID) {
         VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of fetchBundleDescription method");
         String bundleDescription = null;
         VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleID in fetchBundleDescription method is" + bundleID);
         if (null != bundleID) {
            Bundle bundle = null;
            try {
                bundle = ProductSchemaService.getCommercialBundle(bundleID);
            } catch (ProductSchemaException e) {
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Product Schema Exception Came", e);
            }
            if (null != bundle) {
            //Defect fix || CI12 || 52166,WR33098 || start -->
            if (null != bundle.getDisplayName()) {
                   bundleDescription = bundle.getDisplayName();
                   VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundledescription from MEF:"
                                                                                        + bundleDescription);
            } else {
                   bundleDescription = bundle.getPromotionName();
                   VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "bundledescription is PCF"
                                                                        + bundleDescription);
				                                }
             //Defect fix || CI12 || 52166,WR33098 || End -->
             }
         }
         return bundleDescription;
     }
    /**
     * This API will be called when user clicks on Expand secion in Extras/Itemization,
     * which will return deatiled usage details from GetInvoiceDetails TIL.
     * @param accountId String
     * @param itemChargeID String.
     * @param itemType String
     * @param isExtra Boolean
     * @param configListMap Map
     * @param configMap Map
     * @param subscriptionType String
     * @return detailsSubscriptionUsage DetailsSubscriptionUsage
     * @throws WrapBusinessException
     * @throws WrapSystemException
     * //Defect fix || WR32847 || CI 11
     */
    public static final List<DetailsSubscriptionUsage> fetchUsageDetails(final String accountId,
                                                            final String itemChargeID,
                                                            final String itemType, final Boolean isExtra,
                                                            final Map<String, List> configListMap,
                                                            final Map<String, String> configMap,
                                                            final String subscriptionType)
                                                            throws WrapBusinessException, WrapSystemException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "START of fetchInvoiceDetails method");
        List<DetailsSubscriptionUsage> detailsSubUsageList = null;
        GetInvoiceDetailsRequestDTO getInvoiceDetailsRequest = null;
        GetInvoiceDetailsResponseDTO getInvoiceDetailsResponseDTO = null;
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Account Id::" + accountId);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "itemChargeID::" + itemChargeID);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemType::" + itemType);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "isExtra flag::" + isExtra);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ConfigListMap::" + configListMap);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ConfigMap::" + configMap);
        getInvoiceDetailsRequest = BillBreakdownAPIHelper.populateInvoiceDetailsRequest(accountId, itemType,
                                                                                          itemChargeID, isExtra);
        if (null != getInvoiceDetailsRequest) {
            getInvoiceDetailsResponseDTO = getInvoiceDetails(getInvoiceDetailsRequest);
        }
        if (null != getInvoiceDetailsResponseDTO && null != configListMap && !configListMap.isEmpty()
                                    && null != configMap && !configMap.isEmpty()) {
            //Code Change for Defect 53375 | Starts
            detailsSubUsageList = populateGetInvoiceDetailsResponse(getInvoiceDetailsResponseDTO, configListMap,
                                                                    configMap, subscriptionType, itemType);
            //Code Change for Defect 53375 | Ends
        }
        if (null != detailsSubUsageList && !detailsSubUsageList.isEmpty()) {
           for (DetailsSubscriptionUsage detailSubUsage : detailsSubUsageList) {
               VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "DetailsSubscriptionUsageList Values in"
                        + "fetchUsagedetails : " + detailSubUsage.toString());
           }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "END of fetchInvoiceDetails method");
        return detailsSubUsageList;
    }
    /**
     * This method is used to call the GetInvoiceDetails Til call and send back the Response that is
     * received from the TIL.
     * @param invoiceDetailsRequest request containing the accountID,itemChargeId, itemCategory and excludeZeroflag.
     * @return GetInvoiceDetailsResponseDTO received from the GID Til.
     * @throws WrapSystemException
     * @throws WrapBusinessException
     */
    private static GetInvoiceDetailsResponseDTO getInvoiceDetails(final GetInvoiceDetailsRequestDTO
                                        invoiceDetailsRequest) throws WrapSystemException, WrapBusinessException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of getInvoiceDetails method");
        GetInvoiceDetailsResponseDTO invoiceDetailsResponse = null;
        if (null != invoiceDetailsRequest) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "getInvoiceDetails requestDTO is NOT null");
            invoiceDetailsResponse = (GetInvoiceDetailsResponseDTO) ProxyServiceFactory
                                                                .invokeService(invoiceDetailsRequest);
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of getInvoiceDetails method");
        return invoiceDetailsResponse;
    }
    //Code Change for Defect 53375 | Starts
    /**
     * This method populates the Extra Usage Details and Itemized Usage details.
     * from the TIL service GetInvoiceDetails.
     * @param getInvoiceDetailsResponseDTO GetInvoiceDetailsResponseDTO
     * @param configListMap Map
     * @param configMap Map
     * @param subscriptionType String
     * @param usageType String.
     * @return detailsSubUsage
     * //Defect fix || WR32847 || CI 11
     */
    private static final List<DetailsSubscriptionUsage> populateGetInvoiceDetailsResponse(
                                                    final GetInvoiceDetailsResponseDTO getInvoiceDetailsResponseDTO,
                                                    final Map<String, List> configListMap,
                                                    final Map<String, String> configMap,
                                                    final String subscriptionType, final String usageType) {
        //Code Change for Defect 53375 | Ends
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of populateGetInvoiceDetailsResponse method");
        List<DetailsSubscriptionUsage> detailsSubUsageList = null;
        DetailsSubscriptionUsage detailsSubUsage = null;
        if (null != getInvoiceDetailsResponseDTO.getQueryInvoiceListDTOList()) {
            detailsSubUsageList =  new ArrayList<DetailsSubscriptionUsage>();
            for (QueryInvoiceListDTO queryInvoiceListDTO : getInvoiceDetailsResponseDTO.getQueryInvoiceListDTOList()) {
                if (null != queryInvoiceListDTO.getQueryInvoiceListResponse()
                    && null != queryInvoiceListDTO.getQueryInvoiceListResponse().getInvoiceLineDto()) {
                    for (com.vodafone.online.eserv.services.response.getinvoicedetails.InvoiceLineDTO invoiceLineDTO
                          : queryInvoiceListDTO.getQueryInvoiceListResponse().getInvoiceLineDto()) {
                        //Code Change for Defect 53375 | Starts
                        detailsSubUsage = populateInvoiceLine(configListMap, configMap,
                                                              invoiceLineDTO, subscriptionType, usageType);
                        //Code Change for Defect 53375 | Ends
                        detailsSubUsageList.add(detailsSubUsage);
                    }
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "detailsSubUsageList Size : "
                                                                     + detailsSubUsageList.size());
                }
            }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "END of populateGetInvoiceDetailsResponse method");
        return detailsSubUsageList;
    }
    //Code Change for Defect 53375 | Starts
    /**
     * This method will populate the InvoiceLineDTO.
     * @param configListMap Map
     * @param configMap Map
     * @param invoiceLineDTO InvoiceLineDTO
     * @param subscriptionType String
     * @param usageType String.
     * @return detailsSubUsage DetailsSubscriptionUsage
     * //Defect fix || WR32847 || CI 11
     */
    private static DetailsSubscriptionUsage populateInvoiceLine(
                                            final Map<String, List> configListMap, final Map<String, String> configMap,
                                            final com.vodafone.online.eserv.services.response.getinvoicedetails
                                                                .InvoiceLineDTO invoiceLineDTO,
                                            final String subscriptionType, final String usageType) {
        //Code Change for Defect 53375 | Ends
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "START of populateInvoiceLine method");
        String voiceUsageType = configMap.get(BillBreakdownAPIConstants.VOICE_DETAIL_USAGE);
        String smsUsageType = configMap.get(BillBreakdownAPIConstants.SMS_DETAIL_USAGE);
        /* WR30318 || CCS2.0 || Starts */
        String smsUsageType2 = configMap.get(BillBreakdownAPIConstants.SMS_DETAIL_USAGE_2);
//        String subType = null;
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "VoiceDetailUsage :" + " " + voiceUsageType
                            + "SmsDetailUsage :" + smsUsageType + " SmsDetailUsage2 :" + smsUsageType2);
        /* WR30318 || CCS2.0 || Ends */
        SimpleDateFormat dateFormat = new SimpleDateFormat(BillBreakdownAPIConstants.DATE_FORMAT, Locale.UK);
        DateFormat timeFormat = new SimpleDateFormat(BillBreakdownAPIConstants.TIME_FORMAT, Locale.UK);
        DetailsSubscriptionUsage detailsSubUsage = null;
        String itemType = null;
        if (null != invoiceLineDTO && null != invoiceLineDTO.getUsageAllocation()) {
            if (null != invoiceLineDTO.getUsageAllocation().getCustomDto()
                && null != invoiceLineDTO.getUsageAllocation().getCustomDto().getUsageScenario()) {
                itemType = invoiceLineDTO.getUsageAllocation().getCustomDto().getUsageScenario();
            }
             detailsSubUsage = new DetailsSubscriptionUsage();
             if (null != invoiceLineDTO.getUsageAllocation().getPhoneCommunicationDto()
                 && null != invoiceLineDTO.getUsageAllocation().getPhoneCommunicationDto().getCompleteNumber()) {
                 VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "SubscriptionType" + subscriptionType);
                 // TODO calling the method to check for the scenarios for fixed Line connection
                 //Defect fix || WR32847 || CI 11 || Start
                 if (subscriptionType.equalsIgnoreCase(configMap.get(BillBreakdownAPIConstants.FIXED_TYPE))) {
                   String completeNumber = BillBreakdownAPIHelper.changeToCTNFormat(invoiceLineDTO.getUsageAllocation()
                                                .getPhoneCommunicationDto().getCompleteNumber());
                     detailsSubUsage.setDialedNumber(completeNumber);
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "DialedNumber in DetailsSubscriptionUsage in CTN:"
                                                     + detailsSubUsage.getDialedNumber());
                 //Defect fix || WR32847 || CI 11 || End
                 } else {
                     detailsSubUsage.setDialedNumber(invoiceLineDTO.getUsageAllocation()
                                                 .getPhoneCommunicationDto().getCompleteNumber());
                 }
                 VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "DialedNumber in DetailsSubscriptionUsage :"
                                                 + detailsSubUsage.getDialedNumber());
             }
             if (null != invoiceLineDTO.getUsageAllocation().getCustomDto() && null != itemType) {
                 if (null != invoiceLineDTO.getUsageAllocation().getCustomDto().getUsageScenario()) {
                     /* WR30318 || CCS2.0 || Starts */
                     if (itemType.equalsIgnoreCase(voiceUsageType) || itemType.equalsIgnoreCase(smsUsageType)
                         || itemType.equalsIgnoreCase(smsUsageType2)) {
                         /* WR30318 || CCS2.0 || Ends */
                        if (null == invoiceLineDTO.getUsageAllocation().getCustomDto().getDestination()
                             && null == invoiceLineDTO.getUsageAllocation().getCustomDto().getOrigin()) {
                                  detailsSubUsage.setType(invoiceLineDTO.getUsageAllocation().getCustomDto()
                                                                                 .getUsageScenario());
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Type in DetailsSubscriptionUsage"
                                            + "if VOICE/SMS :" + detailsSubUsage.getType());
                        } /* WR30318 || CCS2.0 || Starts */
                         String detailedUsageScenario = invoiceLineDTO.getUsageAllocation().getCustomDto()
                                                                                  .getDetailedUsageScenario();
                         VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "DetailedUsageScenario :"
                                                                          + detailedUsageScenario);
                         if (null != detailedUsageScenario) {
                             List<Map<String, String>> listRows = null;
                             if (itemType.equalsIgnoreCase(voiceUsageType)) {
                                 listRows = configListMap.get(BillBreakdownAPIConstants.VOICE_SUBCATEGORY_LIST);
                             } else if (itemType.equalsIgnoreCase(smsUsageType)
                                        || itemType.equalsIgnoreCase(smsUsageType2)) {
                                 listRows = configListMap.get(BillBreakdownAPIConstants.TEXT_SUBCATEGORY_LIST);
                             }
                             if (null != listRows && !listRows.isEmpty()) {
                                 String subUsageType = null;
                                 for (Map<String, String> row : listRows) {
                                     subUsageType = row.get(BillBreakdownAPIConstants.SUB_USAGE_TYPE);
                                     if (detailedUsageScenario.equalsIgnoreCase(subUsageType)) {
                                         detailsSubUsage.setType(row.get(BillBreakdownAPIConstants.SUB_CATEGORY_NAME));
                                         VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "detailsSubUsage.getType :"
                                                                                          + detailsSubUsage.getType());
                                         break;
                                     }
                                 }
                             }
                         } /* WR30318 || CCS2.0 || Ends */
                     } else {
                     detailsSubUsage.setType(invoiceLineDTO.getUsageAllocation().getCustomDto().getUsageScenario());
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Type in DetailsSubscriptionUsage: "
                                                     + detailsSubUsage.getType());
                     }
                 }
                 if (null != invoiceLineDTO.getUsageAllocation().getCustomDto().getChargedQuantity()) {
                    detailsSubUsage.setDurationMin(BillBreakdownAPIHelper.formatAmountUsedForVoiceUsageInMin(
                            invoiceLineDTO.getUsageAllocation().getCustomDto().getChargedQuantity().toString()));
                    detailsSubUsage.setDurationSec(BillBreakdownAPIHelper.formatAmountUsedForVoiceUsageInSeconds(
                            invoiceLineDTO.getUsageAllocation().getCustomDto().getChargedQuantity().toString()));
                    detailsSubUsage.setVolume(BillBreakdownAPIHelper.formatAountUserForDataUsage(invoiceLineDTO
                                        .getUsageAllocation().getCustomDto().getChargedQuantity().toString()));
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "DurationMin in DetailsSubscriptionUsage :"
                                                     + detailsSubUsage.getDurationMin());
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "DurationSec in DetailsSubscriptionUsage :"
                                                     + detailsSubUsage.getDurationSec());
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Volume in DetailsSubscriptionUsage :"
                                                     + detailsSubUsage.getVolume());
                 }
                 //Defect fix || 51707 || CI11 || start-->
                 if (null != invoiceLineDTO.getUsageAllocation().getPhoneCommunicationDto()
                     && null != invoiceLineDTO.getUsageAllocation().getPhoneCommunicationDto().getCompleteNumber()) {
                    detailsSubUsage.setDestination(invoiceLineDTO.getUsageAllocation()
                                                 .getPhoneCommunicationDto().getCompleteNumber());
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Destination in DetailsSubscriptionUsage :"
                                                     + detailsSubUsage.getDestination());
                     //Defect fix || 51707 || CI11 || End-->
                 } else {
                     detailsSubUsage.setDestination(BillBreakdownAPIConstants.EMPTY);
                 }
                 //Defect Fix 51819 || WR changes handled by Map, hence removed this Snippet here || Starts
                 if (null != invoiceLineDTO.getUsageAllocation().getCustomDto().getStartTimestamp()) {
                     Date compltDate = invoiceLineDTO.getUsageAllocation().getCustomDto().getStartTimestamp().getTime();
                     //Defect Fix || 51906 || CI 11 || Added a logger
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Time before formating::"
                                                     + compltDate.toString());
                     detailsSubUsage.setDate(compltDate);
                     detailsSubUsage.setDisplayDate(dateFormat.format(compltDate));
                     detailsSubUsage.setTime(timeFormat.format(compltDate));
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Date in DetailsSubscriptionUsage :"
                                                     + detailsSubUsage.getDisplayDate());
                     VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Time in DetailsSubscriptionUsage :"
                                                     + detailsSubUsage.getTime());
                 }
             }
                //Defect Fix 51819 || WR changes handled by Map, hence removed this Snippet here || Ends
             if (null != invoiceLineDTO.getUsageAllocation().getTotalAmount()) {
                 double tempCost = BillBreakdownAPIHelper.penceToPoundConversion(invoiceLineDTO.getUsageAllocation()
                                                    .getTotalAmount().toString(), BillBreakdownAPIConstants.HUNDRED);
                 detailsSubUsage.setCost(BillBreakdownAPIHelper.convertToTwoDecPlaces(tempCost));
                 VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Cost in DetailsSubscriptionUsage :"
                                                + detailsSubUsage.getCost());
             }
             if (null != invoiceLineDTO.getUsageAllocation().getDescription()) {
                 detailsSubUsage.setDescription(invoiceLineDTO.getUsageAllocation().getDescription());
                 VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Description in DetailsSubscriptionUsage :"
                                                + detailsSubUsage.getDescription());
             }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "usage  value is" + usageType + configMap
                .get(BillBreakdownAPIConstants.TYPE_CODE_ADJUSTMENT)
                                    + configMap.get(BillBreakdownAPIConstants.DESC_ADJUSTMENT));
            //Code Change for Defect 53375 | Starts
             if (null != configMap.get(BillBreakdownAPIConstants.TYPE_CODE_ADJUSTMENT)
                 && usageType.equalsIgnoreCase(configMap.get(BillBreakdownAPIConstants.TYPE_CODE_ADJUSTMENT))) {
                 detailsSubUsage.setType(configMap.get(BillBreakdownAPIConstants.DESC_ADJUSTMENT));
             } //Code Change for Defect 53375 | Ends
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "END of populateInvoiceLine method");
        return detailsSubUsage;
    }
    /**
     * This method is used to get the SubscriptionMap that we prepared by copairing the subscription present
     * in session and coming from GIS til and then read the configuration map for each of the subscription type
     * and populate the values in InvoiceSummarySubscriptions object and creating the extrausageDetail list and the
     * itemized usage details list and the other subscription details.
     * @param subscriptionMap map of subscriptionID and subscription object present in both session and GIS.s
     * @param configMap map from the taskflow configuration file.
     * @return the map of subscriptioniD as key and InvoiceSummarySubscriptions as the value.
     * @throws ProductSchemaException
     */
    private static final Map<String, InvoiceSummarySubscriptions> populateInvoiceSummarySubscription(
                                                        final Map<String, Subscription> subscriptionMap,
                                                        final Map<String, List> configMap)
                                                        throws ProductSchemaException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "START of populateInvoiceSummarySubscription method");
        Map<String, InvoiceSummarySubscriptions> invoiceSummarySubscriptionMap = null;
        InvoiceSummarySubscriptions invoiceSummarySubs = null;
        Subscription subscription = null;
        List<Map<String, String>> typeCodeList = null;
        UsageDetails usageDetails = null;
        List<UsageDetails> usageDetailList = null;
        if (null != subscriptionMap && null != configMap) {
            invoiceSummarySubscriptionMap = new HashMap<String, InvoiceSummarySubscriptions>();
            for (Map.Entry<String, Subscription> subMap : subscriptionMap.entrySet()) {
                subscription = subMap.getValue();
                if (null != subscription && null != subscription.getSubscriptionType() && null != configMap
                 && null != configMap.get(BillBreakdownAPIConstants.RESULT_VIEW_OPTION + (subscription
                    .getSubscriptionType().replace(BillBreakdownAPIConstants.SPACE,
                                                   BillBreakdownAPIConstants.EMPTY)).toLowerCase())) {
                    typeCodeList = configMap.get(BillBreakdownAPIConstants.RESULT_VIEW_OPTION + (subscription
                                    .getSubscriptionType().replace(BillBreakdownAPIConstants.SPACE,
                                                        BillBreakdownAPIConstants.EMPTY)).toLowerCase());
                    invoiceSummarySubs = new InvoiceSummarySubscriptions();
                    usageDetailList = new ArrayList<UsageDetails>();
                    if (null != typeCodeList) {
                        for (Map<String, String> typeCodeMap : typeCodeList) {
                            usageDetails = new UsageDetails();
                            usageDetails.setSubscriptionTypeCode(typeCodeMap
                                                            .get(BillBreakdownAPIConstants.TIL_VALUE));
                            usageDetails.setSubscriptionUsageType(typeCodeMap
                                                                  .get(BillBreakdownAPIConstants.TYPE));
                            usageDetails.setExtraUsageCharge(BillBreakdownAPIHelper
                                                             .convertToTwoDecPlaces(BigDecimal.valueOf(0)));
                            usageDetailList.add(usageDetails);
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "til value is"
                                    + usageDetails.getSubscriptionTypeCode() + "type value is"
                                        + usageDetails.getSubscriptionUsageType());
                        }
                        invoiceSummarySubs.setExtraUsageDetails(usageDetailList);
                        invoiceSummarySubs.setSubscriptionID(subscription.getSerialNumber());
                        invoiceSummarySubs.setSubscriptionType(subscription.getSubscriptionType());
                        //Code changes for WR33296 | Starts
                        //invoiceSummarySubs.setUsingAccountID(subscription.getUsingAccountId());
                        //Code changes for WR33296 | Ends
//                        if (null != subscription.getBundleId()) {
//                            invoiceSummarySubs.setSubBundleDescription(
//                                    fetchBundleDescriptionFromCoherence(subscription.getBundleId()));
//                        }
//                        if (null != subscription.getReferenceName()
//                                        && !subscription.getReferenceName().isEmpty()) {
//                            invoiceSummarySubs.setSubDescription(subscription.getReferenceName());
//                        } else {
                            invoiceSummarySubs.setSubDescription(subscription.getSubscriptionType());
//                        }
                    }
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "serialnumer put into map is"
                                                                      + subscription.getSerialNumber());
                    invoiceSummarySubscriptionMap.put(subscription.getSerialNumber(), invoiceSummarySubs);
                }
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "invoiceSummarySubscriptionMap map is"
                                                              + invoiceSummarySubscriptionMap);
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of populateInvoiceSummarySubscription method");
        return invoiceSummarySubscriptionMap;
    }
    //Code changes for WR33296 | Starts
    /**
     * This method is used to check if the suscription that we are getting from GIS response is of inactive or
     * an active subscription.
     * @param inactiveCtn String.
     * @return booelean -true if an inactive subscription else false.
     */
    private static final boolean checkForInactiveSubscription(final String inactiveCtn) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "STart of checkForInactiveSubscription method");
        if (null != inactiveCtn && inactiveCtn.contains("_")) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "serial number is of inactive subscription");
            return Boolean.TRUE;
        } else {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "serial number is not of inactive subscription");
            return Boolean.FALSE;
        }
    }
    //Code changes for WR33296 | Ends
    /**
     * This method is used to check if the subscription id coming from GIS til is present in session or not.
     * If it is present in session then it return the subscription object otherwise return null;
     * @param ctn subscriptionID from invoiceline coming from GIS.
     * @param subscriptionList list of all subscription present under the active account.
     * @return Subscription object if present in both otherwise null.
     */
    private static final Subscription fetchSubscription(final String ctn,
                                            final List<Subscription> subscriptionList) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Inside of fetchSubscription method");
        Subscription subscrptn = null;
        String formattedCtn = null;
        if (null != ctn && null != subscriptionList) {
            //defect 52346 || start
            formattedCtn = BillBreakdownAPIHelper.changeCTNFormat(ctn);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "ctn " + ctn
                                                + " formattedCtn  " + formattedCtn);
            for (Subscription subscription : subscriptionList) {
                if (null != subscription && null != subscription.getSerialNumber()
                    && (subscription.getSerialNumber().equalsIgnoreCase(formattedCtn)
                    || subscription.getSerialNumber().equalsIgnoreCase(ctn))) {
                    subscrptn = subscription;
                    break;
                }
                //defect 52346 || end

            }
        }
        return subscrptn;
    }
    /**
     * This method will return the InvoiceType List for the Typecodes Cycle-Forward, Refund, Adjustment or Purchase Fee.
     * @param invoiceSummaryResponse GetInvoiceSummaryResponseDTO
     * @param configMapListType Map
     * @return invoiceTypeList List
     * CR changes || 51407 || CI 11
     */
    private static final List<InvoiceType> getInvoiceTypeList(final GetInvoiceSummaryResponseDTO invoiceSummaryResponse,
                                                              final Map<String, List> configMapListType) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of getInvoiceTypeList method");
        List<InvoiceType> invoiceTypeList = null;
        InvoiceType invoiceType = null;
        if (null != configMapListType && !configMapListType.isEmpty()) {
        List<Map<String, String>> tilTypeGrp =
            (List<Map<String, String>>) configMapListType.get(BillBreakdownAPIConstants.TIL_TYPE_CODE);
        Map<String, String> typeCodeDescpMap = new HashMap<String, String>();
        if (null != invoiceSummaryResponse.getQueryinvoicelistresponse()
            && null != invoiceSummaryResponse.getQueryinvoicelistresponse().getInvoiceline()) {
            invoiceTypeList = new ArrayList<InvoiceType>();
            for (InvoiceLineDTO invoiceLine
                 : invoiceSummaryResponse.getQueryinvoicelistresponse().getInvoiceline()) {
                if (null != invoiceLine.getIdentificationDTO()
                    && null != invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO()
                    && (null == invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId()
                    || invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId().isEmpty())) {
                    if (null != invoiceLine.getItemreference()
                        && null != invoiceLine.getItemreference().getTypecode()
                        && null != tilTypeGrp && !tilTypeGrp.isEmpty()) {
                        invoiceType = new InvoiceType();
                        for (Map<String, String> typeCodeMap : tilTypeGrp) {
                            typeCodeDescpMap.put(typeCodeMap.get(BillBreakdownAPIConstants.TYPE_CODE),
                                                 typeCodeMap.get(BillBreakdownAPIConstants.TYPE_DESCRIPTION));
                        }
                        for (Map.Entry<String, String> entry : typeCodeDescpMap.entrySet()) {
                            if (invoiceLine.getItemreference().getTypecode().equalsIgnoreCase(entry.getKey())) {
                                if (null != invoiceLine.getIdentificationDTO()
                                    && null != invoiceLine.getIdentificationDTO().getApplicationObjectKeyDTO()
                                    && null != invoiceLine.getIdentificationDTO()
                                               .getApplicationObjectKeyDTO().getId()) {
                                    invoiceType.setItemChargeID(invoiceLine.getIdentificationDTO()
                                                                .getApplicationObjectKeyDTO().getId());
                                    invoiceType.setItemChargeType(invoiceLine.getItemreference().getTypecode());
                                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemChargeId is "
                                                       + invoiceType.getItemChargeID());
                                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "TypeCode is "
                                                        + invoiceType.getItemChargeType());
                                }
                                if (null != invoiceLine.getExtendedAmount()) {
                                    invoiceType.setCost(invoiceLine.getExtendedAmount());
                                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Cost is "
                                                    + invoiceType.getCost());
                                }
                                invoiceTypeList.add(invoiceType);
                            }
                        }
                    } else {
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO,
                                           "Type Code is NULL");
                    }
                }
            }
        } else {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO,
                               "Invoice Line is Null");
        }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO,
                           "End of getInvoiceTypeList method");
        return invoiceTypeList;
    }
    /**
     * This method will return the InvoiceAccountCharge List for the Typecodes Cycle-Forward or Refund or Adjustment
     * or Purchase Fee, by iterating each invoice Line in GetInvoiceDetails TIL service.
     * @param accountId String
     * @param itemChargeId String.
     * @param itemType String
     * @param isExtra Boolean
     * @param configMap Map
     * @return invoiceTypeList List
     */
    public static final List<InvoiceAccountCharge> getInvoiceAccountCharge(final String accountId, final String
                                                            itemChargeId, final String itemType, final Boolean isExtra,
                                                                           final Map<String, String> configMap)
                                                            throws WrapSystemException, WrapBusinessException,
                                                                        ProductSchemaException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of getInvoiceAccountCharge method");
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Account Id:: " + accountId);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "itemChargeID:: " + itemChargeId);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemType:: " + itemType);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "isExtra flag:: " + isExtra);
        List<InvoiceAccountCharge> invoiceAccountChargeList = null;
        InvoiceAccountCharge invoiceAccountCharge = null;
        GetInvoiceDetailsRequestDTO getInvoiceDetailsRequest = null;
        GetInvoiceDetailsResponseDTO getInvoiceDetailsResponseDTO = null;
        if (null != accountId && null != itemType && null != itemChargeId) {
            getInvoiceDetailsRequest = BillBreakdownAPIHelper.populateInvoiceDetailsRequest(accountId, itemType,
                                                                                                itemChargeId, false);
        } else {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "accountId or itemType or itemChargeID is null");
        }
        if (null != getInvoiceDetailsRequest) {
            getInvoiceDetailsResponseDTO = getInvoiceDetails(getInvoiceDetailsRequest);
        }
        if (null != getInvoiceDetailsResponseDTO && null != getInvoiceDetailsResponseDTO.getQueryInvoiceListDTOList()) {
            invoiceAccountChargeList = new ArrayList<InvoiceAccountCharge>();
            for (QueryInvoiceListDTO queryInvcListDTO : getInvoiceDetailsResponseDTO.getQueryInvoiceListDTOList()) {
                if (null != queryInvcListDTO.getQueryInvoiceListResponse()
                    && null != queryInvcListDTO.getQueryInvoiceListResponse().getInvoiceLineDto()) {
                    for (com.vodafone.online.eserv.services.response.getinvoicedetails.InvoiceLineDTO invoiceLineDTO
                         : queryInvcListDTO.getQueryInvoiceListResponse().getInvoiceLineDto()) {
                            invoiceAccountCharge = new InvoiceAccountCharge();
                            invoiceAccountCharge.setDescription(fetchDescription(invoiceLineDTO, configMap, itemType));
                            invoiceAccountCharge.setValue(fetchCostFromGID(invoiceLineDTO));
                            invoiceAccountChargeList.add(invoiceAccountCharge);
                    }
                }
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "invoiceAccountChargeList Size : "
                                                                            + invoiceAccountChargeList.size());
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of getInvoiceAccountCharge method");
        return invoiceAccountChargeList;
    }
    /**
    * This method is used to fetch the product description by calling the getCommercialBundle API which
    * internally calls the coherence and get the bundleBundle description.
    * @param productId for the subscription.
    * @param configMap Map.
    * @return the product Description that it gets from coherence.
    * @throws ProductSchemaException
    */
    private static final String fetchProductDescriptionFromCoherence(final String productId,
                                                                     final Map<String, String> configMap)
                                                                     throws ProductSchemaException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of fetchProductDescriptionFromCoherence method");
        String productDescription = null;
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "productId in fetchBundleDescription method is" + productId);
        if (null != productId) {
            Product product = null;
            product = ProductSchemaService.getProduct(productId);
            if (null != product) {
            //CR31003 || Starts
                //WR32460 || ProductDescription for MultiRoom Product Fetch name from Canonical || Starts
                //D52323 || Added null check to avoid null pointer exception -CI12|| Starts
               // NC78| CCS 4.0 | AQUA| LCS Phase 1 - Single Line Rental_V0.3 PAY08 changes |START
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "productLineList:::" + product.getProductLineList());
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "product.getProductClass():::"
                                                                  + product.getProductClass());
                if (null != configMap && null != configMap.get(BillBreakdownAPIConstants.PRODUCT_CLASS_NAME)
                    && configMap.get(BillBreakdownAPIConstants.PRODUCT_CLASS_NAME)
                    .equalsIgnoreCase(BillBreakdownAPIConstants.BUNDLE_CLASS)
                    && configMap.get(BillBreakdownAPIConstants.PRODUCT_CLASS_NAME)
                    .equalsIgnoreCase(product.getProductClass())
                       && null != product.getPriceDetail() && BillBreakdownAPIConstants.PRICE_TYPE_RECURRING
                           .equalsIgnoreCase(product.getPriceDetail().getPriceType())
                              && null != product.getProductLineList()
                                && product.getProductLineList().contains("Line Rental")) {
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "NC78 LCS verification "
                                                   + configMap.get(BillBreakdownAPIConstants.PRODUCT_CLASS_NAME)
                                                   + configMap.get(BillBreakdownAPIConstants.BUNDLE_NAME));
                    productDescription = configMap.get(BillBreakdownAPIConstants.BUNDLE_NAME);
                    } else if (null != configMap && null != configMap.get(BillBreakdownAPIConstants.PRODUCT_CLASS_NAME)
                    && configMap.get(BillBreakdownAPIConstants.PRODUCT_CLASS_NAME)
                    .equalsIgnoreCase(product.getProductClass())
                       && null != product.getPriceDetail() && BillBreakdownAPIConstants.PRICE_TYPE_RECURRING
                           .equalsIgnoreCase(product.getPriceDetail().getPriceType())) {
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, " LCS verification --"
                                                   + configMap.get(BillBreakdownAPIConstants.PRODUCT_CLASS_NAME)
                                                   + configMap.get(BillBreakdownAPIConstants.BUNDLE_NAME));
                        productDescription = configMap.get(BillBreakdownAPIConstants.BUNDLE_NAME);
                        // NC78| CCS 4.0 | AQUA| LCS Phase 1 - Single Line Rental_V0.3 PAY08 changes | END
                    } else {
                        productDescription = product.getProductName();
                    }
                //D52323 || Added null check to avoid null pointer exception -CI12|| Ends
                //WR32460 || ProductDescription for MultiRoom & Product Fetch name from Canonical || Ends
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "productDescription is" + productDescription);
            //CR31003 || end
            } else {
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Product is not present in coherence");
            }
        } else {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Product Id is null");
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of fetchProductDescriptionFromCoherence method");
        return productDescription;
    }
    /**
     * This method is used to get the itemChargeID from the InvocieLine that we get from GIS.
     * @param invoiceLine coming from GIS.
     * @return String the itemchargeid that we get from GIS..
     */
    private static final String fetchItemChargeID(final InvoiceLineDTO invoiceLine) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Inside of fetchItemChargeID method");
        String itemChargeID = null;
        if (null != invoiceLine && null != invoiceLine.getIdentificationDTO()
            && null !=  invoiceLine.getIdentificationDTO().getApplicationObjectKeyDTO()) {
            itemChargeID = invoiceLine.getIdentificationDTO()
                                            .getApplicationObjectKeyDTO().getId();
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "itemchargeid is:-"
                                        + itemChargeID);
        }
        return itemChargeID;
    }
    /**
     * This method is used to get the Bundle/Discount charge and Description by taking the invoiceLine and
     * accountID as the parameter and it calls the GID til to get the description and then it populates the
     * BundleDetail object and retutns back the response.
     * @param invoiceLine invoice line for the cycle-forward node to check the amount is negative or positive.
     * @param configMap Map which gives the type-code configured.
     * @return InvoiceType which will have the itemchargeid,typecode,bundlecharge for the subscription' bundle charge.
     */
    private static final InvoiceType fetchBundleCharges(final InvoiceLineDTO invoiceLine,
                                                        final Map<String, String> configMap) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of fetchBundleCharges method");
        InvoiceType bundleInvocieType = null;
        double bundleCharge;
        if (null != invoiceLine && null != configMap
            && null != configMap.get(BillBreakdownAPIConstants.TYPE_CODE_CYCLE_FORWARD)
            && null != invoiceLine.getItemreference() && null != invoiceLine.getItemreference().getTypecode()
            && invoiceLine.getItemreference().getTypecode().equalsIgnoreCase(configMap
                                                    .get(BillBreakdownAPIConstants.TYPE_CODE_CYCLE_FORWARD))) {
            bundleInvocieType = new InvoiceType();
            bundleInvocieType.setItemChargeType(invoiceLine.getItemreference().getTypecode());
            if (null != invoiceLine.getIdentificationDTO().getApplicationObjectKeyDTO()
                && null != invoiceLine.getIdentificationDTO().getApplicationObjectKeyDTO().getId()) {
                bundleInvocieType.setItemChargeID(invoiceLine.getIdentificationDTO()
                                                  .getApplicationObjectKeyDTO().getId());
            }
            if (null != invoiceLine.getExtendedAmount()) {
                bundleCharge = BillBreakdownAPIHelper.penceToPoundConversion(invoiceLine
                                .getExtendedAmount().toString(), BillBreakdownAPIConstants.HUNDRED);
                bundleInvocieType.setCost(BillBreakdownAPIHelper.convertToTwoDecPlaces(bundleCharge));
            }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of fetchBundleAndDiscountCharges method");
        return bundleInvocieType;
    }
    /**
     * This method will return the BundleDetails List for the Typecodes by iterating each invoice Line
     * in GetInvoiceDetails TIL service.
     * @param accountId String
     * @param itemChargeId String.
     * @param itemType String
     * @param isExtra Boolean
     * * @param configMap Map
     * @return bundleDetailsList List
     */
    public static final List<BundleDetails> getBundleDetails(final String accountId, final String
                                                            itemChargeId, final String itemType, final Boolean isExtra,
                                                             final Map<String, String> configMap)
                                                            throws WrapSystemException, WrapBusinessException,
                                                                    ProductSchemaException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of getBundleDetails method");
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Account Id:- " + accountId);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "itemChargeID:- " + itemChargeId);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemType:- " + itemType);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "isExtra flag:- " + isExtra);
        List<BundleDetails> bundleDetailsList = null;
        BundleDetails bundleDetails = null;
        GetInvoiceDetailsRequestDTO getInvoiceDetailsRequest = null;
        GetInvoiceDetailsResponseDTO getInvoiceDetailsResponseDTO = null;
        if (null != accountId && null != itemType && null != itemChargeId) {
            getInvoiceDetailsRequest = BillBreakdownAPIHelper.populateInvoiceDetailsRequest(accountId, itemType,
                                                                                                itemChargeId, false);
        } else {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "accountId or itemType or itemChargeID is null:");
        }
        if (null != getInvoiceDetailsRequest) {
            getInvoiceDetailsResponseDTO = getInvoiceDetails(getInvoiceDetailsRequest);
        }
        if (null != getInvoiceDetailsResponseDTO && null != getInvoiceDetailsResponseDTO.getQueryInvoiceListDTOList()) {
            bundleDetailsList = new ArrayList<BundleDetails>();
            String bundleStrtDate = null;
            String bundleEndDate = null;
            //Added noOfMultiRooms, bundleName for WR32460 || start
            int noOfMultiRooms = 1;
            String bundleName = null;
            //Added noOfMultiRooms, bundleName for WR32460 || end
            for (QueryInvoiceListDTO queryInvcListDTO : getInvoiceDetailsResponseDTO.getQueryInvoiceListDTOList()) {
                if (null != queryInvcListDTO.getQueryInvoiceListResponse()
                    && null != queryInvcListDTO.getQueryInvoiceListResponse().getInvoiceLineDto()) {
                    for (com.vodafone.online.eserv.services.response.getinvoicedetails.InvoiceLineDTO invoiceLineDTO
                         : queryInvcListDTO.getQueryInvoiceListResponse().getInvoiceLineDto()) {
                        bundleDetails = new BundleDetails();
                        // WR32460 || Start
                        bundleName = fetchDescription(invoiceLineDTO, configMap, itemType);
                        if (bundleName.equalsIgnoreCase(BillBreakdownAPIConstants.MULTIROOM)) {
                        bundleDetails.setSubBundleName(bundleName + noOfMultiRooms);
                        noOfMultiRooms++;
                        } else {
                        bundleDetails.setSubBundleName(bundleName);
                        }
                        // WR32460 || End
                        bundleDetails.setSubscriptionCharges(fetchCostFromGID(invoiceLineDTO));

                        //WR31038 || Starts
                        if (null != invoiceLineDTO.getUsageAllocation() && null != invoiceLineDTO.getUsageAllocation()
                                                                                                      .getStartDate()) {
                        bundleDetails.setBundleStartDate(invoiceLineDTO.getUsageAllocation()
                                                      .getStartDate().getTime());
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleDetails.getBundleStartDate"
                                                                        + bundleDetails.getBundleStartDate());
                        // Set new start date with date format
                        bundleStrtDate = formatDateToDisplay(bundleDetails.getBundleStartDate().toString()
                                  , BillBreakdownAPIConstants.DATE_TO_FORMATEE, BillBreakdownAPIConstants.DATE_FORMAT);
                        bundleDetails.setDispBundleStartDate(bundleStrtDate);
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, " bundleDetails.getDispBundleStartDate"
                                                                          + bundleDetails.getDispBundleStartDate());
                            }
                    if (null != invoiceLineDTO.getUsageAllocation() && null != invoiceLineDTO.getUsageAllocation()
                      .getCustomDto() && null != invoiceLineDTO.getUsageAllocation().getCustomDto().getEndTimestamp()) {
                        bundleDetails.setBundleEndDate(invoiceLineDTO.getUsageAllocation().getCustomDto()
                                                                                        .getEndTimestamp().getTime());
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleDetails.getBundleEndDate"
                                                                          + bundleDetails.getBundleEndDate());
                        // Set new start date with date format
                        bundleEndDate = formatDateToDisplay(bundleDetails.getBundleEndDate().toString()
                                  , BillBreakdownAPIConstants.DATE_TO_FORMATEE, BillBreakdownAPIConstants.DATE_FORMAT);
                        bundleDetails.setDispBundleEndDate(bundleEndDate);
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, " bundleDetails.getDispBundleEndDate"
                                                                          + bundleDetails.getDispBundleEndDate());
                      }
                        bundleDetailsList.add(bundleDetails);
                    }
                }
            }
            Collections.sort(bundleDetailsList, new Comparator<BundleDetails>() {
                    public int compare(final BundleDetails bundleDetails1,
                                       final BundleDetails bundleDetails2) {
                        if (null != bundleDetails1.getBundleStartDate()
                            && null != bundleDetails2.getBundleStartDate()) {
                            return bundleDetails1.getBundleStartDate().compareTo(bundleDetails2.getBundleStartDate());
                        }
                        return 0;
                    }
                });
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleDetailsList Size::::: " + bundleDetailsList.size());
            //Logic to Group Charges based on Date Starts
            List<BigDecimal> subscriptionChargeTmpList;
            List<String> subscriptionBundleName;
            // <WR35985 mid billing period - to pull credit for service || Start>
            Map<String, List<BigDecimal>> bundleChargeMap;
            List<BigDecimal> outerBundleChargesList;
            List<BigDecimal> innerBundleChargesList;
            // <WR35985 mid billing period - to pull credit for service || End>
                for (int base = 0; base < bundleDetailsList.size(); base++) {
                    //null check added for bd3 defect 51750 ||start
                    //Defect fix || 52036 || condition change || start -->
                   if (bundleDetailsList.get(base).getSubcriptionflagset() != 1
                   && null != bundleDetailsList.get(base).getSubscriptionCharges()
                   && bundleDetailsList.get(base).getSubscriptionCharges().compareTo(BigDecimal.ZERO) != 0) {
                        //Defect fix || 52036 || condition change || End -->
                        //null check added for bd3 defect 51750 ||end
                        subscriptionChargeTmpList = new ArrayList<BigDecimal>();
                        subscriptionBundleName = new ArrayList<String>();
                        // <WR35985 mid billing period - to pull credit for service|| Start>
                        bundleChargeMap = new HashMap<String, List<BigDecimal>>();
                        outerBundleChargesList = new ArrayList<BigDecimal>();
                        outerBundleChargesList.add(bundleDetailsList.get(base).getSubscriptionCharges());
                        // <WR35985 mid billing period - to pull credit for service|| End>
                        subscriptionChargeTmpList.add(bundleDetailsList.get(base).getSubscriptionCharges());
                        //Added for WR32460 sortMRProduct || Starts
                        if (bundleDetailsList.get(base).getSubBundleName().toLowerCase().contains(
                                                                            BillBreakdownAPIConstants.MULTIROOM)) {
                        subscriptionBundleName.add(BillBreakdownAPIConstants.MULTI_DISP_NAME);
                            // <WR35985 mid billing period - to pull credit for service|| Start>
                        bundleChargeMap.put(BillBreakdownAPIConstants.MULTI_DISP_NAME
                                                               , outerBundleChargesList);
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleChargeMap contains -: "
                                                                + outerBundleChargesList);
                            // <WR35985 mid billing period - to pull credit for service|| End>
                        } else {
                            subscriptionBundleName.add(bundleDetailsList.get(base).getSubBundleName());
                            // <WR35985 mid billing period - to pull credit for service|| Start>
                            bundleChargeMap.put(bundleDetailsList.get(base).getSubBundleName()
                                                                , outerBundleChargesList);
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleChargeMap contains : "
                                                                + outerBundleChargesList);
                            // <WR35985 mid billing period - to pull credit for service|| End>
                        }
                        //Added for WR32460 sortMRProduct || Ends
                    for (int nextvar = base + 1; nextvar < bundleDetailsList.size(); nextvar++) {
                    //null check added for bd3 defect 51750 ||start
                        if (null != bundleDetailsList.get(base).getBundleStartDate()
                            && null != bundleDetailsList.get(nextvar).getBundleStartDate()
                            && null != bundleDetailsList.get(base).getBundleEndDate()
                            && null != bundleDetailsList.get(nextvar).getBundleEndDate()) {
                          //null check added for bd3 defect 51750 ||end
                        if (bundleDetailsList.get(base).getBundleStartDate().getTime()
                            == bundleDetailsList.get(nextvar).getBundleStartDate().getTime()
                            && bundleDetailsList.get(base).getBundleEndDate().getTime()
                             == bundleDetailsList.get(nextvar).getBundleEndDate().getTime()) {
                          //Defect fix || 52036 || condition change || start -->
                            if (bundleDetailsList.get(nextvar).getSubscriptionCharges()
                                          .compareTo(BigDecimal.ZERO) != 0) {
                            //Defect fix || 52036 || condition change || End -->
                             //Added for WR32460 sortMRProduct || Starts
                            subscriptionChargeTmpList.add(bundleDetailsList.get(nextvar).getSubscriptionCharges());
                            // <WR35985 mid billing period - to pull credit for service|| Start>
                            innerBundleChargesList = new ArrayList<BigDecimal>();
                            // <WR35985 mid billing period - to pull credit for service|| End>
                            if (bundleDetailsList.get(nextvar).getSubBundleName().toLowerCase().contains(
                                                                            BillBreakdownAPIConstants.MULTIROOM)) {
                            subscriptionBundleName.add(BillBreakdownAPIConstants.MULTI_DISP_NAME);
                            // <WR35985 mid billing period - to pull credit for service|| Start>
                            if (bundleChargeMap.containsKey(BillBreakdownAPIConstants.MULTI_DISP_NAME)) {
                                innerBundleChargesList = bundleChargeMap.get(BillBreakdownAPIConstants.MULTI_DISP_NAME);
                                innerBundleChargesList.add(bundleDetailsList.get(nextvar).getSubscriptionCharges());
                                } else {
                                innerBundleChargesList.add(bundleDetailsList.get(nextvar).getSubscriptionCharges());
                            }
                            if (innerBundleChargesList != null) {
                            bundleChargeMap.put(BillBreakdownAPIConstants.MULTI_DISP_NAME
                                                        , innerBundleChargesList);
                            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleChargeMap consists -: "
                                                                    + innerBundleChargesList);
                            }
                                // <WR35985 mid billing period - to pull credit for service|| End>
                            bundleDetailsList.get(nextvar).setSubcriptionflagset(1);
                            } else {
                                subscriptionBundleName.add(bundleDetailsList.get(nextvar).getSubBundleName());
                                // <WR35985 mid billing period - to pull credit for service|| Start>
                                if (bundleChargeMap.containsKey(bundleDetailsList.get(nextvar).getSubBundleName())) {
                                    innerBundleChargesList = bundleChargeMap.get(bundleDetailsList.get(nextvar)
                                                                                 .getSubBundleName());
                                    innerBundleChargesList.add(bundleDetailsList.get(nextvar).getSubscriptionCharges());
                                    } else {
                                    innerBundleChargesList.add(bundleDetailsList.get(nextvar).getSubscriptionCharges());
                                    }
                                if (innerBundleChargesList != null) {
                                bundleChargeMap.put(bundleDetailsList.get(nextvar).getSubBundleName()
                                                    , innerBundleChargesList);
                                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleChargeMap consists : "
                                                                            + innerBundleChargesList);
                                }
                                // <WR35985 mid billing period - to pull credit for service || End>
                                bundleDetailsList.get(nextvar).setSubcriptionflagset(1);
                            }
                            //Added for WR32460 sortMRProduct || Ends
                        }
                      }
                      }
                    }
                        bundleDetailsList.get(base).setSubBundleNameList(subscriptionBundleName);
                        bundleDetailsList.get(base).setSubscriptionChargesList(subscriptionChargeTmpList);
                        bundleDetailsList.get(base).setBundlechargeMap(bundleChargeMap);
                    }
                }
            for (int bndlDetail = 0; bndlDetail < bundleDetailsList.size(); bndlDetail++) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleDetailsList Size -: "
                                                        + bundleDetailsList.get(bndlDetail).getBundleStartDate());
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleDetailsList Size -:: "
                                                            + bundleDetailsList.get(bndlDetail).getBundleEndDate());
                if (null != bundleDetailsList.get(bndlDetail).getSubscriptionChargesList()
                    && !bundleDetailsList.get(bndlDetail).getSubscriptionChargesList().isEmpty()
                    && null != bundleDetailsList.get(bndlDetail).getSubBundleNameList()
                    && !bundleDetailsList.get(bndlDetail).getSubBundleNameList().isEmpty()) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleDetailsList Size -::: "
                                    + bundleDetailsList.get(bndlDetail).getSubscriptionChargesList().toString());
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "bundleDetailsList Size -:::: "
                                    + bundleDetailsList.get(bndlDetail).getSubBundleNameList().toString());
                }
            }
            //WR31038 || Ends
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of getBundleDetails method");
        return bundleDetailsList;
    }
    /**
     * This method is used to get the Description if available in GID otherwise it takes the ProductID field from
     * GID and get the name of the Product from COherence.
     * @param invoiceLine InvoiceLIne coming from GID.
     * @param configMap Map
     * @param itemType String
     * @return description fetched from either GID or Coherence.
     */
    private static final String fetchDescription(final com.vodafone.online.eserv.services.response
                                                 .getinvoicedetails.InvoiceLineDTO invoiceLine,
                                                 final Map<String, String> configMap, final String itemType)
                                                 throws ProductSchemaException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of fetchDescription method");
        String description = null;
        String productId = null;
//      String alterntObjtIdentity = null;
        // Defect Fix 51377 || WCP CI 11 ||  CR WR31003 || Start
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Config Map" + configMap);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "itemType " + itemType);
//        if (null != invoiceLine.getUsageAllocation()) {
//            if (null != invoiceLine.getUsageAllocation().getDescription() && !invoiceLine.getUsageAllocation()
//                                .getDescription().isEmpty()
//                && !itemType.equalsIgnoreCase(configMap.get("type_code_purchase_fee"))) {
//                description = invoiceLine.getUsageAllocation().getDescription();
//                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Description::; "
//                                                            + description);
//            } else if
             if (null != invoiceLine.getUsageAllocation()
                 && null != invoiceLine.getUsageAllocation().getIdentificationDTO()
                  && null != invoiceLine.getUsageAllocation().getIdentificationDTO().getAlternateObjectKeyDTO()
                   && null != invoiceLine.getUsageAllocation().getIdentificationDTO().getAlternateObjectKeyDTO()
                    .getIdentity()) {
//               alterntObjtIdentity = invoiceLine.getUsageAllocation().getIdentificationDTO()
//                    .getAlternateObjectKeyDTO().getIdentity();
//                       VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ProductId::;; " + alterntObjtIdentity);
//            if (alterntObjtIdentity.contains(BillBreakdownAPIConstants.UNDER_SCORE)) {
//                    productId = StringUtils.substringBefore(alterntObjtIdentity,
//                                                            BillBreakdownAPIConstants.UNDER_SCORE);
                     productId = invoiceLine.getUsageAllocation().getIdentificationDTO()
                     .getAlternateObjectKeyDTO().getIdentity();
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ProductId::;; " + productId);
                    //CR31003 || Start
                   // NC78| CCS 4.0 | AQUA| LCS Phase 1 - Single Line Rental_V0.3 PAY08 changes |start
                    description = fetchProductDescriptionFromCoherence(productId, configMap);
                   // NC78| CCS 4.0 | AQUA| LCS Phase 1 - Single Line Rental_V0.3 PAY08 changes | end
                    //CR31003 || End
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Description from Coherence "
                                                        + description);
                   }
                    // Defect Fix 51377 || WCP CI 11 ||  CR WR31003 || End
//                }
//            }
//        } else {
//            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "UsageAllocation is NULL");
//        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of fetchDescription method" + description);
        return description;
    }
    /**
     * This method is used to get the TotalCost from TotalAmount field of GID if it is not null other wise it
     * return the cost as 0.
     * @param invoiceLine Invoice line coming from GID.
     * @return Total cost in Bigdecimal format.
     */
    private static final BigDecimal fetchCostFromGID(final com.vodafone.online.eserv.services.response
                                                     .getinvoicedetails.InvoiceLineDTO invoiceLine) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of fetchCostFromGID");
        BigDecimal totalCost = null;
        double totalAmount;
        if (null != invoiceLine.getUsageAllocation() && null != invoiceLine.getUsageAllocation().getTotalAmount()) {
                totalAmount = BillBreakdownAPIHelper.penceToPoundConversion(invoiceLine
                        .getUsageAllocation().getTotalAmount().toString(),
                                                BillBreakdownAPIConstants.HUNDRED);
                totalCost = BillBreakdownAPIHelper.convertToTwoDecPlaces(totalAmount);
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "totalCost::::: " + totalCost);
        } else {
            totalCost = BillBreakdownAPIHelper.convertToTwoDecPlaces(Double.valueOf(0));
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of fetchCostFromGID" + totalCost);
        return totalCost;
    }
    //WR31038 || Starts
    /**
     * This method is used to format the input date to display in view.
     *  @param inputDate String
     *  @param inputDateFormat String
     *  @param reqFormat String
     * @return formattedDate String
    */
    public static final String formatDateToDisplay(final String inputDate,
                                         final String inputDateFormat, final String reqFormat) {
       VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "start of formatDateToDisplay");
            String formattedDate = null;
            try {
            SimpleDateFormat formatter = new SimpleDateFormat(inputDateFormat, Locale.ENGLISH);
            Date parsedDate = formatter.parse(inputDate);
            SimpleDateFormat  simpleDateFormat = new SimpleDateFormat(reqFormat, Locale.ENGLISH);
            formattedDate = simpleDateFormat.format(parsedDate);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "formattedDate: " + formattedDate);
            } catch (ParseException pe) {
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_ERROR, "ParseException :", pe);
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "end of formatDateToDisplay");
            return formattedDate;
        }
    //WR31038 || Ends


    /**
     * This is the API that will be called from the BillBreakdown taskflow from the default activity which
     * will call the GetInvoiceSummary Til by passing the serviceAccountID, BillingProfileID and the invoiceID
     * coming from the PAY02 or PAY04 as request and then populating the Response for each of the subscription
     * present in the session and then the API will sort the Subscription based on the configured value in config
     * file.This API will aslo call the GetInvoiceDetails TIL for getting the bundleDescription and the Discount
     * description and also the account charge information.
     * @param serviceAccountID incontext account id to call GIS til.
     * @param invoiceID invoice ID coming from either PAY02 or Pay04.
     * @param billingProfileID incontext billing profile id coming from session.

     * @return AccountDetails object which will contain the List of accountcharge object and Map of
     * subscription ID as the key and InvoiceSummarySubscriptions object as the value.
     * @throws WrapSystemException
     * @throws WrapBusinessException
     */
    public static final AccountDetails getBillSummary(final String serviceAccountID,
                                                final String invoiceID, final String billingProfileID)
                                                throws WrapSystemException, WrapBusinessException,
                                                ProductSchemaException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of getBillSummary method");
        AccountDetails accountDetails = null;
        GetInvoiceSummaryRequestDTO invoiceSummaryRequest = null;
        GetInvoiceSummaryResponseDTO invoiceSummaryResponse = null;
        Map<String, Subscription> filteredSubscription = null;
        Map<String, InvoiceSummarySubscriptions> invoiceSummaryMap = null;
        List<InvoiceType> invoiceTypeList = null;
        Map configMapListType;
        Map<String, String> configMap = null;
        //Preparing the request for calling the GetInvoiceSummary Til.
        invoiceSummaryRequest = BillBreakdownAPIHelper.prepareRequestForGetInvoiceSummary(serviceAccountID,
                                                                                    invoiceID, billingProfileID);
        //Calling the GetInvoiceSummary TIL by passing the request generated in previous step.
        if (null != invoiceSummaryRequest) {
        invoiceSummaryResponse = getInvoiceSummary(invoiceSummaryRequest);
        }
        if (null != invoiceSummaryResponse) {
            //Get the invoiceTypeList for type code cycle-forward/Adjustment/Purchase fee/Refund
            //Retrieving the subscritionList that will be displayed on Pay08 taskflow by taking only those subscription
            //present in both session and GIS til.

            //TODO HARD CODE Subscriptions

//            Subscription sub1 = new Subscription();
//
//            sub1.setSerialNumber("447786005719");
//            sub1.setSubscriptionType("Mobile");
//            sub1.setUsingAccountId("");
//
//            filteredSubscription.put( sub1.getSerialNumber(), sub1);
//
//            Subscription sub2 = new Subscription();
//
//            sub2.setSerialNumber("447786005718");
//            sub2.setSubscriptionType("Mobile");
//            sub2.setUsingAccountId("");
//
//            filteredSubscription.put( sub2.getSerialNumber(), sub2);


            filteredSubscription = getSubscriptionList(invoiceSummaryResponse,serviceAccountID);
            // CR || CI11 || 51407 || Start

            configMapListType = getDocumentListValues("df_newco_pay08_ebill_config_v3.xml");
            configMap = prepareConfigMapForTypeCode();
//            try {
//                configMap = (HashMap<String, String>) ResourceLoaderSingleton.getInstance()
//                    .getResource(Integer.valueOf(BaseConstants.RESOURCE_TYPE.getValue()),
//                                BaseConstants.RESOURCE_LOCATION.getValue(), "df_newco_pay08_ebill_config_v3");
//            } catch (ResourceLoadException res) {
//                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_ERROR,
//                             "ResourceLoadException Occured while Loading the xml file: ", res);
//            }
            invoiceTypeList = getInvoiceTypeList(invoiceSummaryResponse, configMapListType);

            // CR || CI11 || 51407 || End
            if (null != filteredSubscription) {


                //Populating the GetInvoiceSummary response
                invoiceSummaryMap = prepareInvoiceSummary(filteredSubscription,
                                            invoiceSummaryResponse, configMapListType, configMap);
                accountDetails = new AccountDetails();
                accountDetails.setCurrentInvoiceSubscriptionSummary(invoiceSummaryMap);
                //Getting the Account charge information from GIS and GID til by passing the GIS response and
                //billingAccountID.
                //accountCharges = retrieveInvoiceAccountCharge(configMap, invoiceSummaryResponse, serviceAccountID);
                //accountDetails.setInvoiceAccountCharges(accountCharges);
                accountDetails.setInvoiceTypeList(invoiceTypeList);
            }
        }
        if (null != accountDetails) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "AccountDetails in fetchBillSymmaryAndAccDetails"
                                                + accountDetails.toString());
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of fetchBillSymmaryAndAccDetails method");
        return accountDetails;
    }
    public static final Map<String, List<Map<String, String>>> getDocumentListValues(final String documentId) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Starts getDocumentListValues()::::::::::::::::::::::::");
        XmlObject xmlObject = null;
        String key = null;
        final Map<String, List<Map<String, String>>> cacheMap = new HashMap<String, List<Map<String, String>>>();
        ListRowType[] rows;
        List<Map<String, String>> allRowsList;
        Map<String, String> myRowMap;
        ElementType[] elements;
        RootDocument soDoc = null;
        InputStream inputStream = null;
        XMLMessageProcessorInterface xmpInterface =
            new XMLMessageProcessor();
        try {
            xmlObject = (XmlObject) ResourceLoaderSingleton.getInstance().getResource(ResourceConstants.XML_TYPE,
                                                                      ResourceConstants.FILE_LOCATION,
                                                                      documentId);
            inputStream = new ByteArrayInputStream(xmlObject.toString().getBytes(Charset.forName("UTF-8")));
            soDoc = (RootDocument) xmpInterface.getDocument(inputStream);
            for (ListType listType : soDoc.getRoot().getListArray()) {
                key = listType.getName();
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Processing list with name [" + key
                                                                 + "]");
                rows =
                    listType.getRowArray();
                allRowsList = new ArrayList<Map<String, String>>();
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Found rows with size [" + rows.length
                                                                 + "]");
                for (ListRowType listRowType : rows) {
                    myRowMap =
                        new HashMap<String, String>();
                    elements = listRowType.getElementArray();
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Found elements with size ["
                                                                     + elements.length + "]");
                    for (ElementType elementType : elements) {
                        myRowMap.put(elementType.getName(), elementType.getStringValue());
                    }
                    allRowsList.add(myRowMap);
                }
                cacheMap.put(key, allRowsList);
            }
        } catch (VFXmlException exp) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "VFXmlException Exception occured ::::" + exp);
        } catch (ResourceLoadException resExp) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                               "ResourceLoadException Exception occured ::::"
                               + resExp);
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Ends getDocumentListValues ():::::::::::::::::::::::::::");
        return cacheMap;
    }
    private static final Map<String, List> prepareConfigMap() {
        Map<String, List> configMapList = new HashMap<String, List>();
        List<Map> typeCodeMapList = null;
        Map<String, String> usageMap = null;
        usageMap = new HashMap<String, String>();
        typeCodeMapList = new ArrayList<Map>();
        usageMap.put(TIL_VALUE, VOICE_USAGE);
        usageMap.put(BillBreakdownAPIConstants.TYPE, CALLS);
        typeCodeMapList.add(usageMap);
        usageMap = new HashMap<String, String>();
        usageMap.put(TIL_VALUE, SMS_USAGE);
        usageMap.put(BillBreakdownAPIConstants.TYPE, MESSAGING);
        typeCodeMapList.add(usageMap);
        usageMap = new HashMap<String, String>();
        usageMap.put(TIL_VALUE, DATA_USAGE);
        usageMap.put(BillBreakdownAPIConstants.TYPE, DATA);
        typeCodeMapList.add(usageMap);
        usageMap = new HashMap<String, String>();
        usageMap.put(TIL_VALUE, OTHER_USAGE);
        usageMap.put(BillBreakdownAPIConstants.TYPE, "Other");
        typeCodeMapList.add(usageMap);
        configMapList.put("results_view_options_mobile", typeCodeMapList);
        usageMap = new HashMap<String, String>();
        typeCodeMapList = new ArrayList<Map>();
        usageMap.put(TIL_VALUE, VOICE_USAGE);
        usageMap.put(BillBreakdownAPIConstants.TYPE, CALLS);
        typeCodeMapList.add(usageMap);
        usageMap = new HashMap<String, String>();
        usageMap.put(TIL_VALUE, DATA_USAGE);
        usageMap.put(BillBreakdownAPIConstants.TYPE, DATA);
        typeCodeMapList.add(usageMap);
        configMapList.put("results_view_options_mbb", typeCodeMapList);
        usageMap = new HashMap<String, String>();
        typeCodeMapList = new ArrayList<Map>();
        usageMap.put(TIL_VALUE, VOICE_USAGE);
        usageMap.put(BillBreakdownAPIConstants.TYPE, CALLS);
        typeCodeMapList.add(usageMap);
        configMapList.put("results_view_options_fixedline", typeCodeMapList);
        usageMap = new HashMap<String, String>();
        typeCodeMapList = new ArrayList<Map>();
        usageMap.put(TIL_VALUE, TV_USAGE);
        usageMap.put(BillBreakdownAPIConstants.TYPE, TV_PURCHASES);
        typeCodeMapList.add(usageMap);
        configMapList.put("results_view_options_tvservice", typeCodeMapList);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "configMapList " + configMapList);
        return configMapList;
    }

    private static List<Subscription> getSubscrptionList() {
        List<Subscription> allSubscriptionList = new ArrayList<Subscription>();

        Subscription sub1 = new Subscription();

        sub1.setSerialNumber("447786005719");
        sub1.setSubscriptionType("Mobile");
        sub1.setUsingAccountId("315607033");

        allSubscriptionList.add(sub1);

        Subscription sub2 = new Subscription();

        sub2.setSerialNumber("447786005718");
        sub2.setSubscriptionType("Mobile");
        sub2.setUsingAccountId("315607033");

        allSubscriptionList.add(sub2);

        return allSubscriptionList;
    }

    /**
     * This method is used to get the List of all the Subscription that are present in both
     * under GIS til and all the subscription List present user the active account.
     * @param invoiceSummaryResponse Response Received from the GIS til.
     * @param accountID serviceaccount id.
     * @param iOnlineUtilities used for session building when called from junit.
     * @return List of only those Subscription that are present in Session and GIS both.
     */
    private static Map<String, Subscription> getSubscriptionList(final GetInvoiceSummaryResponseDTO
                                                invoiceSummaryResponse, final String accountID)
                                                throws WrapSystemException, WrapBusinessException {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of retrieveApplicableSubscriptionList method");
        List<Subscription> allSubscriptionList = null;
        Map<String, Subscription> filteredSubscriptionMap = null;
        Subscription subscription = null;
        //Code changes for WR33296 | Starts
        Map<String, Subscription> inactiveSubscriptionMap = null;
        String formattedSerialNumber = null;
        List<String> inactiveSubscriptions = null;
        //Code changes for WR33296 | Ends
        //Getting all the subscription under the active account from SessionContextSubscriptionAPI.
          allSubscriptionList = getSubscrptionList();
        //If the GIS response is not null and the subscriptionList is not null then comparing the subscription
        //present in session with the subscription coming from GIS Til and preparing the list of only those
        //Subscription that are Present in Session and discarding rest.
        if (null != allSubscriptionList && null != invoiceSummaryResponse
            && null != invoiceSummaryResponse.getQueryinvoicelistresponse()
                && null != invoiceSummaryResponse.getQueryinvoicelistresponse().getInvoiceline()) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "SubscriptionList from Session"
                                                              + allSubscriptionList.size());
            //Code changes for WR33296 | Starts
            filteredSubscriptionMap = new HashMap<String, Subscription>();
            inactiveSubscriptions = new ArrayList<String>();
            for (InvoiceLineDTO invoiceLine : invoiceSummaryResponse
                                    .getQueryinvoicelistresponse().getInvoiceline()) {
                if (null != invoiceLine && null != invoiceLine.getIdentificationDTO()
                    && null != invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO()
                    && null != invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId()
                    && !invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId().isEmpty()) {
                    if (checkForInactiveSubscription(invoiceLine.getIdentificationDTO()
                                                            .getAlternativeObjectKeyDTO().getId())) {
                        formattedSerialNumber = BillBreakdownAPIHelper.changeInactiveCTNFormat(
                                        invoiceLine.getIdentificationDTO().getAlternativeObjectKeyDTO().getId());
                        inactiveSubscriptions.add(formattedSerialNumber);
                    } else {
                        subscription = fetchSubscription(invoiceLine.getIdentificationDTO()
                                                .getAlternativeObjectKeyDTO().getId(), allSubscriptionList);
                        if (null != subscription) {
                            filteredSubscriptionMap.put(subscription.getSerialNumber(), subscription);
                        }
                    }
                }
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "SubscriptionMap after filtering"
                                                          + filteredSubscriptionMap.size());
//            if (!inactiveSubscriptions.isEmpty() && accountID != null) {
//                inactiveSubscriptionMap = retrieveInactiveSubscriptionFromGIPL(inactiveSubscriptions, accountID);
//                if (null != inactiveSubscriptionMap && !inactiveSubscriptionMap.isEmpty()) {
//                    filteredSubscriptionMap.putAll(inactiveSubscriptionMap);
//                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
//                        "SubscriptionMap after adding inactive subscription" + filteredSubscriptionMap.size());
//                }
//            }
            //Code changes for WR33296 | Ends
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of retrieveApplicableSubscriptionList method");
        return filteredSubscriptionMap;
    }

    /**
     * prepareConfigMapForTypeCode to put the values in configMap.
     */
    private static final  Map<String, String> prepareConfigMapForTypeCode() {
        Map<String, String> configMap;
        configMap = new HashMap<String, String>();
        configMap.put(BillBreakdownAPIConstants.VOICE_DETAIL_USAGE, CALLS);
        configMap.put(BillBreakdownAPIConstants.SMS_DETAIL_USAGE, MESSAGING);
        configMap.put(BillBreakdownAPIConstants.DATA_DETAIL_USAGE, DATA);
        configMap.put(BillBreakdownAPIConstants.TV_DETAIL_USAGE, TV_PURCHASES);
        configMap.put(BillBreakdownAPIConstants.VOICE_USAGE_TYPE, VOICE_USAGE);
        configMap.put(BillBreakdownAPIConstants.SUB_USAGE_TYPE, SMS_USAGE);
        configMap.put(BillBreakdownAPIConstants.DATA_USAGE_TYPE, DATA_USAGE);
        configMap.put("other_usage_type", OTHER_USAGE);
        configMap.put(BillBreakdownAPIConstants.TV_USAGE_TYPE, TV_USAGE);
        configMap.put(BillBreakdownAPIConstants.TYPE_CODE_CYCLE_FORWARD, BillBreakdownAPIConstants.CYCLE_FORWARD);
        configMap.put(BillBreakdownAPIConstants.TYPE_CODE_ADJUSTMENT, BillBreakdownAPIConstants.ADJUSTMENT);
        configMap.put("type_code_purchase_fee", BillBreakdownAPIConstants.PURCHASE_FEE);
        configMap.put("type_code_refund", BillBreakdownAPIConstants.REFUND);
        configMap.put("desc_refund", "Refunded Payments");
        configMap.put("desc_adjustment", "Description From Adjustments");
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "configMap " + configMap);
        return configMap;
    }

    /**
     * Holds String accountID.
     */
    private static final String ACCOUNT_ID = "3000000091";
    /**
     * Holds String itemChargeId.
     */
    private static final String ITEMCHARGE_ID = "0.0.0.1 /item/data_usage 116505937 0";
    /**
     * Holds String cycle_forward.
     */
    private static final String CYCLE_FORWARD = "cycle_forward";
    /**
     * Holds String til_value.
     */
    private static final String TIL_VALUE = "till_value";
    /**
     * Holds String Voice-Usage.
     */
    private static final String VOICE_USAGE = "Voice-Usage";
    /**
     * Holds String Voice-Usage.
     */
    private static final String OTHER_USAGE = "Other-Usage";
    /**
     * Holds String Calls.
     */
    private static final String CALLS = "Calls";
    /**
     * Holds String Messaging.
     */
    private static final String MESSAGING = "Messaging";
    /**
     * Holds String Data.
     */
    private static final String DATA = "Data";
    /**
     * Holds String TV-Usage.
     */
    private static final String TV_USAGE = "TV-Usage";
    /**
     * Holds String Data-Usage.
     */
    private static final String DATA_USAGE = "Data-Usage";
    /**
     * Holds String TV purchases.
     */
    private static final String TV_PURCHASES = "TV purchases";
        /**
     * Holds String TV purchases.
     */
    private static final String SMS_USAGE = "SMS-Usage";



}
