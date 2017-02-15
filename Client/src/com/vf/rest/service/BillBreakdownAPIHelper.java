package com.vf.rest.service;


import com.vodafone.online.eserv.constants.BillBreakdownAPIConstants;
import com.vodafone.online.eserv.constants.SesInitializationConstants;
import com.vodafone.online.eserv.constants.SubscriptionConstants;
import com.vodafone.online.eserv.framework.utility.common.StringHelper;
import com.vodafone.online.eserv.framework.utility.common.StringUtil;
import com.vodafone.online.eserv.framework.utility.logger.VodafoneLogger;
import com.vodafone.online.eserv.helper.sessioncontext.SessionContextSubscriptionDetailHelper;
import com.vodafone.online.eserv.managedbeans.sessioncontext.Subscription;
import com.vodafone.online.eserv.services.request.getinvoicedetails.GetInvoiceDetailsRequestDTO;
import com.vodafone.online.eserv.services.request.getinvoicesummary.GetInvoiceSummaryRequestDTO;
import com.vodafone.online.eserv.services.request.getinvoicesummary.QueryCriteriaDTO;
import com.vodafone.online.eserv.services.request.getinvoicesummary.QueryDTO;
import com.vodafone.online.eserv.services.request.serviceCaseList.QueryExpressionDTO;
import com.vodafone.online.eserv.services.request.serviceCaseList.ValueExpressionDTO;

import com.vodafone.online.eserv.services.response.getinstalledproductslist.GetInstalledProductListResponseDTO;

import com.vodafone.online.eserv.services.response.getinstalledproductslist.InstalledProductCustomerDTO;
import com.vodafone.online.eserv.services.response.getinstalledproductslist.QueryInstalledProductListResponseDTO;

import com.vodafone.online.eserv.services.response.getinstalledproductslist.QueryResponseDataAreaTypeDTO;

import com.vodafone.online.eserv.services.response.getinstalledproductslist.RelatedInstalledProductDTO;

import java.math.BigDecimal;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/** =========================================================================
 * This is a BillBreakdownAPIHelper class.
 *
 * @author: HP Company.
 ** @reason:
 * ==========================================================================
 */
public final class BillBreakdownAPIHelper {
    /**
     * Constructor.
     */
    private BillBreakdownAPIHelper() {
        super();
    }
    /**
     * This method takes the Incontext AccountID, BillingProfileID and the invoiceID passed from the
     * BillBreakdown taskflow.Invoice Id will be either from the previousBill or Current Bill.
     * Based on this parameter this method will create the request object required to call the
     * GIS Til.
     * @param accountID service AccountID passed from the currentBill or the Previousbill taskflow.
     * @param invoiceID passed from the previous or current bill taskflow
     * @param billingProfileID incontext billingProfileID present in the session.
     * @return request to be passed to call the GetInvoiceSummary TIl.
     */
    public static final GetInvoiceSummaryRequestDTO prepareRequestForGetInvoiceSummary(final String accountID,
                                                        final String invoiceID, final String billingProfileID) {
        GetInvoiceSummaryRequestDTO invoiceSummaryRequest = null;
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Starting prepareRequestForGetInvoiceSummary method");
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Account Id::" + accountID);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "BillingProfile ID::" + billingProfileID);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Invoice ID::" + invoiceID);
        ValueExpressionDTO veDTO = null;
        List<ValueExpressionDTO> veExprsnDTOList = null;
        QueryDTO queryDTO = null;
        QueryExpressionDTO qeDTO = null;
        QueryCriteriaDTO queryCriteriaDTO = null;
        List<QueryDTO> quryDTOList = null;
        invoiceSummaryRequest = new GetInvoiceSummaryRequestDTO();
        invoiceSummaryRequest.setServiceName(BillBreakdownAPIConstants.GET_INVOICE_SUMMARY_SERVICE);
        veExprsnDTOList = new ArrayList<ValueExpressionDTO>();
        if (null != accountID) {
            veDTO = new ValueExpressionDTO();
            veDTO.setElementPath(BillBreakdownAPIConstants.ACCOUNT_ID);
            veDTO.setValue(accountID);
            veDTO.setQueryOperatorCode(ValueExpressionDTO.QueryOperatorCode.EQUALS);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ElementPath::" + veDTO.getValue()
                                                              + veDTO.getElementPath());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Value:: "
                                                              + veDTO.getValue());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "QueryOperatorCode ::"
                                                              + veDTO.getQueryOperatorCode().EQUALS);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ElementPath :" + veDTO.getValue() + "Value :"
                                                              + veDTO.getElementPath());
            veExprsnDTOList.add(veDTO);
        }
        if (null != billingProfileID) {
            veDTO = new ValueExpressionDTO();
            veDTO.setElementPath(BillBreakdownAPIConstants.BILLING_PROFILE_ID);
            veDTO.setValue(billingProfileID);
            veDTO.setQueryOperatorCode(ValueExpressionDTO.QueryOperatorCode.EQUALS);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ElementPath:::" + veDTO.getValue()
                                                              + veDTO.getElementPath());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Value::: "
                                                              + veDTO.getValue());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "QueryOperatorCode :::"
                                                              + veDTO.getQueryOperatorCode().EQUALS);
            veExprsnDTOList.add(veDTO);
        }
         if (null != invoiceID) {
            veDTO = new ValueExpressionDTO();
            veDTO.setElementPath(BillBreakdownAPIConstants.INVOICE_ID);
            veDTO.setValue(invoiceID);
            veDTO.setQueryOperatorCode(ValueExpressionDTO.QueryOperatorCode.EQUALS);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ElementPath:" + veDTO.getValue()
                                                              + veDTO.getElementPath());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Value: "
                                                                + veDTO.getValue());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "QueryOperatorCode :"
                                                                + veDTO.getQueryOperatorCode().EQUALS);
            veExprsnDTOList.add(veDTO);
        }
        qeDTO = new QueryExpressionDTO();
        qeDTO.setLogicalOperatorCode(qeDTO.getLogicalOperatorCode().AND);
        qeDTO.setValueExpressionDTOList(veExprsnDTOList);
        queryCriteriaDTO = new QueryCriteriaDTO();
        queryCriteriaDTO.setQueryexpressions(qeDTO);
        queryDTO = new QueryDTO();
        queryDTO.setQuerycriteria(queryCriteriaDTO);
        queryDTO.setResponsecode(BillBreakdownAPIConstants.FULL_RESPONSE);
        quryDTOList = new ArrayList<QueryDTO>();
        quryDTOList.add(queryDTO);
        invoiceSummaryRequest.setQuerydto(quryDTOList);
        List<ValueExpressionDTO> veDTOl = null;
        veDTOl = invoiceSummaryRequest.getQuerydto().get(0).getQuerycriteria()
                                    .getQueryexpressions().getValueExpressionDTOList();
        for (ValueExpressionDTO valueDTO : veDTOl) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Queries : ElementPath :: "
                     + valueDTO.getElementPath() + " -> " + "Value ;" + valueDTO.getValue());
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of prepareRequestForGetInvoiceSummary method");
        return invoiceSummaryRequest;
    }
    /**
     * The method will convert pence to pound.
     * @param penceValue String.
     * @param conversionFactor String.
     * @return double.
     */
    public static final double penceToPoundConversion(final String penceValue,
                                               final String conversionFactor) {
        return Double.valueOf(penceValue)
            / (Integer.valueOf(conversionFactor));
    }
    /**
     * @param value Object.
     * @return BigDecimal.
     */
    public static final BigDecimal convertToTwoDecPlaces(final Object value) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of  convertToTwoDecPlaces method");
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "value before converting to 2 decimal" + value);
        if (null != value) {
            BigDecimal bigDecvalue = new BigDecimal(value.toString());
            String value1Formatted = decimalFormat.format(bigDecvalue);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Value after formatting :: "
                                    + value1Formatted);
            return new BigDecimal(value1Formatted);
        }
        return null;
    }
    //Code changes for WR33296 | Starts
    /**
     * This method is used to remove the 44 from the Serial number that we receive from GIS til.
     * @param ctn String that is received from GIS til.
     * @return formatted Serial Number which is starting with 0 and not 44.
     */
    public static final String changeCTNFormat(final String ctn) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of  changeSubscriptionFormat method");
        String formattedCtn = null;
        String ctn1 = ctn;
        char [] ctnArray;
        //WR32760 | CI11 | start
        if (ctn1.contains("_")) {
            String[] splitctn = ctn1.split("_");
            ctn1 = splitctn[0];
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "splited ctn" + ctn1);
            }
        if (null != ctn1 && !ctn1.isEmpty() && ctn1.startsWith(BillBreakdownAPIConstants.FOURTY_FOUR)) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "CTN is starting with 44" + ctn1);
            ctnArray = ctn1.toCharArray();
            ctnArray[0] = ' ';
            ctnArray[1] = '0';
            formattedCtn = String.valueOf(ctnArray);
            formattedCtn = formattedCtn.trim();
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "CTN starting with 44 replaced with 0: " + ctn1);
            return formattedCtn;
        }
        return ctn1;
    }
    /**
     * This method is used to remove the underscore from the Serial number that we receive from GIS til.
     * @param ctn String that is received from GIS til.
     * @return formatted Serial Number.
     */
    public static final String changeInactiveCTNFormat(final String ctn) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of  changeInactiveCTNFormat method");
        String ctn1 = ctn;
        if (ctn1.contains("_")) {
            String[] splitctn = ctn1.split("_");
            ctn1 = splitctn[0];
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "splited ctn is" + ctn1);
            }
        return ctn1;
    }
    //Code changes for WR33296 | Ends
    /**
     * This method takes the Incontext AccountID, ItemCategory and the itemChargeID and excludeZeroCost
     * flag passed from the BillBreakdown taskflow.
     * Based on these parameter this method will create the request object required to call the GID Til.
     * @param accountId String
     * @param itemCategory String
     * @param itemChargeID String
     * @param excludeZeroCost Boolean
     * @return getInvoiceDetailsRequestDTO GetInvoiceDetailsRequestDTO.
     */
    public static final GetInvoiceDetailsRequestDTO populateInvoiceDetailsRequest(final String accountId,
                                                            final String itemCategory, final String itemChargeID,
                                                            final Boolean excludeZeroCost) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "START of populateInvoiceDetailsRequest method");
        GetInvoiceDetailsRequestDTO getInvoiceDetailsRequestDTO = new GetInvoiceDetailsRequestDTO();
        com.vodafone.online.eserv.services.request.serviceCaseList.QueryDTO queryDTO
            = new com.vodafone.online.eserv.services.request.serviceCaseList.QueryDTO();
        List<com.vodafone.online.eserv.services.request.serviceCaseList.QueryDTO> queryDTOList
            = new ArrayList<com.vodafone.online.eserv.services.request.serviceCaseList.QueryDTO>();
        com.vodafone.online.eserv.services.request.serviceCaseList.QueryCriteriaDTO querycriteria
            = new com.vodafone.online.eserv.services.request.serviceCaseList.QueryCriteriaDTO();
        QueryExpressionDTO queryexpressions = new QueryExpressionDTO();
        List<ValueExpressionDTO> valueExpressionDTOList = new ArrayList<ValueExpressionDTO>();
        ValueExpressionDTO valueExpressionDTO = null;
        queryDTO.setResponseCode(BillBreakdownAPIConstants.FULL_RESPONSE);
        if (null != accountId && !accountId.isEmpty()) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Account ID" + accountId);
            valueExpressionDTO = new ValueExpressionDTO();
            valueExpressionDTO.setQueryOperatorCode(ValueExpressionDTO.QueryOperatorCode.EQUALS);
            valueExpressionDTO.setElementPath(BillBreakdownAPIConstants.ACCOUNT_ID);
            valueExpressionDTO.setValue(accountId);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "AccountId - ElementPath::"
                                                                    + valueExpressionDTO.getElementPath());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "AccountId - Value:: " + valueExpressionDTO.getValue());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "AccountId - QueryOperatorCode ::"
                                                              + valueExpressionDTO.getQueryOperatorCode().EQUALS);
            valueExpressionDTOList.add(valueExpressionDTO);
        }
        if (null != itemCategory && !itemCategory.isEmpty()) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemCategory" + itemCategory);
            valueExpressionDTO = new ValueExpressionDTO();
            valueExpressionDTO.setQueryOperatorCode(ValueExpressionDTO.QueryOperatorCode.EQUALS);
            valueExpressionDTO.setElementPath(BillBreakdownAPIConstants.ITEM_CATEGORY);
            valueExpressionDTO.setValue(itemCategory);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemCategory - ElementPath::"
                                                             + valueExpressionDTO.getElementPath());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemCategory - Value:: " + valueExpressionDTO.getValue());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemCategory - QueryOperatorCode ::"
                                                              + valueExpressionDTO.getQueryOperatorCode().EQUALS);
            valueExpressionDTOList.add(valueExpressionDTO);
        }
        if (null != itemChargeID && !itemChargeID.isEmpty()) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemChargeID::" + itemChargeID);
            valueExpressionDTO = new ValueExpressionDTO();
            valueExpressionDTO.setQueryOperatorCode(ValueExpressionDTO.QueryOperatorCode.EQUALS);
            valueExpressionDTO.setElementPath(BillBreakdownAPIConstants.ITEM_CHARGE_ID);
            valueExpressionDTO.setValue(itemChargeID);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemChargeID - ElementPath::"
                                                             + valueExpressionDTO.getElementPath());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemChargeID - Value:: " + valueExpressionDTO.getValue());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ItemChargeID - QueryOperatorCode ::"
                                                              + valueExpressionDTO.getQueryOperatorCode().EQUALS);
            valueExpressionDTOList.add(valueExpressionDTO);
        }
        if (null != excludeZeroCost) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "excludeZeroCost" + excludeZeroCost);
            valueExpressionDTO = new ValueExpressionDTO();
            valueExpressionDTO.setQueryOperatorCode(ValueExpressionDTO.QueryOperatorCode.EQUALS);
            valueExpressionDTO.setElementPath(BillBreakdownAPIConstants.EXCLUDE_ZERO_COST);
            valueExpressionDTO.setValue(excludeZeroCost.toString());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ExcludeZeroCost - ElementPath::"
                                                             + valueExpressionDTO.getElementPath());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ExcludeZeroCost - Value:: "
                                                             + valueExpressionDTO.getValue());
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ExcludeZeroCost - QueryOperatorCode ::"
                                                              + valueExpressionDTO.getQueryOperatorCode().EQUALS);
            valueExpressionDTOList.add(valueExpressionDTO);
        }
        queryexpressions.setValueExpressionDTOList(valueExpressionDTOList);
        querycriteria.setQueryExpressionDTO(queryexpressions);
        queryDTO.setQueryCriteriaDTO(querycriteria);
        queryDTOList.add(queryDTO);
        getInvoiceDetailsRequestDTO.setQueries(queryDTOList);
        getInvoiceDetailsRequestDTO.setServiceName(BillBreakdownAPIConstants.GET_INVOICE_DETAILS_SERVICE);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Service name is : "
                                                         + getInvoiceDetailsRequestDTO.getServiceName());
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "END of populateInvoiceDetailsRequest method");
        return getInvoiceDetailsRequestDTO;
    }
    /**
     * This method will return the sub category name which will be used to display on page.
     * @param configMap
     *          askflow configuration file entries of format String, String.
     * @param configListMap
     *          Taskflow configuration file entries of format String, List.
     * @param tillUsageType
     *          Usage type got from TIL.
     * @param shortUsageType
     *          The short form of usage type retruned from TIL. If TIL wont retrun this then passs NULL value.
     *          As per NC64 TIL may ot may not return this short usage type.
     *          Ex: NTEL, ITEL, STEL, etc....
     * @param completeNumber
     *          completeNumber returned by TIL.
     * @param isRoaming
     *          Tells whether the complete number is roaming or not.
     * @return finalSubCategoryName
     *          Sub category name to be diaplyed on page.
     */
    public static String getSubType(final Map<String, String> configMap, final Map<String, List> configListMap,
                                    final String tillUsageType, final String shortUsageType,
                                    final String completeNumber, final boolean isRoaming) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "getSubType start");
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "tillUsageType = " + tillUsageType);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "completeNumber = " + completeNumber);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "isRoaming = " + isRoaming);
        List<Map<String, String>> listRows;
        String pattern = null;
        String prefixPattern = null;
        String subUsageType = null;
        String subCategoryName = null;
        String finalSubCategoryName = null;
        //This section will check whether the number is prefixed with 44 or not, 44 pattern will be read from config
        //file
        boolean isPrefixMatch = false;
        String prefixKey = getConfigKeyForGIDUsageScenario(configMap, tillUsageType, isRoaming, true);
        if (null != configListMap && !configListMap.isEmpty() && null != tillUsageType && !tillUsageType.isEmpty()
            && null != completeNumber && !completeNumber.isEmpty() && null != prefixKey && !prefixKey.isEmpty()) {
            listRows = configListMap.get(prefixKey);
            if (null != listRows && !listRows.isEmpty()) {
                //Assuming there will be only one row for prefix check lists
                for (Map<String, String> row : listRows) {
                    subUsageType = row.get(BillBreakdownAPIConstants.SUB_USAGE_TYPE);
                    prefixPattern = row.get(BillBreakdownAPIConstants.PATTERN);
                    subCategoryName = row.get(BillBreakdownAPIConstants.SUB_CATEGORY_NAME);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "subUsageType == " + subUsageType);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "prefixPattern == " + prefixPattern);
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "subCategoryName == " + subCategoryName);
                    //If the config file does not have any entries for this then it means we should skip the 44 check
                    if (null != prefixPattern && prefixPattern.isEmpty()) {
                        isPrefixMatch = true;
                    } else if (null != prefixPattern && !prefixPattern.isEmpty()
                               && completeNumber.startsWith(prefixPattern)) {
                        isPrefixMatch = true;
                    } else {
                        finalSubCategoryName = subCategoryName;
                    }
                }
            }
        } else {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_ERROR, "Any one among configFileMap, tillUsageType , "
                    + "tillUsageType or configKey is NULL or empty .");
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "isPrefixMatch = " + isPrefixMatch);
        //new
        //Once the prefix is matched then we need to check for the dialled number patterns
        if (isPrefixMatch) {
            String configListKey = getConfigKeyForGIDUsageScenario(configMap, tillUsageType, isRoaming, false);
            if (null != configListMap && !configListMap.isEmpty() && null != tillUsageType && !tillUsageType.isEmpty()
                && null != completeNumber && !completeNumber.isEmpty() && null != configListKey
                && !configListKey.isEmpty()) {
                listRows = configListMap.get(configListKey);
                if (null != listRows && !listRows.isEmpty()) {
                    for (Map<String, String> row : listRows) {
                        subUsageType = row.get(BillBreakdownAPIConstants.SUB_USAGE_TYPE);
                        pattern = row.get(BillBreakdownAPIConstants.PATTERN);
                        subCategoryName = row.get(BillBreakdownAPIConstants.SUB_CATEGORY_NAME);
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "subUsageType = " + subUsageType);
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "pattern = " + pattern);
                        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "subCategoryName = " + subCategoryName);
                        finalSubCategoryName = getSubCategoryName(shortUsageType, subUsageType, subCategoryName,
                                                                  completeNumber, pattern, prefixPattern);
                        if (null != finalSubCategoryName && !finalSubCategoryName.isEmpty()) {
                            break; //NOPMD
                        }
                    }
                }
            } else {
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_ERROR, "Any one among configFileMap, tillUsageType, "
                    + "tillUsageType or configKey is NULL or empty.");
            }
            //old
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "finalSubCategoryName = " + finalSubCategoryName);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "getSubType end");
        return finalSubCategoryName;
    }
    /**
     * This method will check whether the given shortUsageType from TIL matches the subUsageType from config file.
     * If matched then the subCategoryName will be returned.
     * Else it will matach the completeNumber with the passed parameter and if the pattern matched then it will return
     * the corresponding subCategoryName from config file.
     * @param shortUsageType
     *          The short form of usage type retruned from TIL. If TIL wont retrun this then passs NULL value.
     *          As per NC64 TIL may ot may not return this short usage type.
     *          Ex: NTEL, ITEL, STEL, etc....
     * @param subUsageType
     *          The subUsageType got from the config file for a given row in the list.
     * @param subCategoryName
     *          The subCategoryName got from the config file for a given row in the list.
     * @param completeNumber
     *          Complete number got from till reponse.
     * @param pattern
     *          Pattern to be checked against the completeNumber.
     * @param prefixPattern
     *          Pattern to be checked for 44 prefix.
     * @return finalSubCategoryName
     *          Sub category name to be diaplyed on page.
     */
    public static String getSubCategoryName(final String shortUsageType, final String subUsageType,
                                            final String subCategoryName, final String completeNumber,
                                            final String pattern, final String prefixPattern) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "getSubCategoryName start");
        String finalSubCategoryName = null;
        //if the shortUsageType is retruned by TIL then it takes preference over complete number
        if (null != shortUsageType && !shortUsageType.isEmpty() && null != subUsageType
            && !subUsageType.isEmpty() && shortUsageType.equalsIgnoreCase(subUsageType)) {
            finalSubCategoryName = subCategoryName;
        //if the shortUsageType is NOT retruned by TIL then complete number will needs to be checked
        } else if (null != pattern && !pattern.isEmpty()) {
            if (isPatternMatched(completeNumber, pattern, prefixPattern)) {
                finalSubCategoryName = subCategoryName;
            } else {
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_ERROR, "pattern did not match");
            }
        } else {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_ERROR, "shortUsgaeType and pattern is NULL");
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "getSubCategoryName end");
        return finalSubCategoryName;
    }
    /**
     * This method will check if the completeNumber present in parameter matches the pattern passed as paraeter
     * to the method.
     * @param completeNumber
     *          Complete number got from till reponse.
     * @param pattern
     *          Pattern to be checked against the completeNumber.
     * @param prefixPattern
     *          Pattern to be checked for 44 prefix.
     * @return boolean
     *          true if the pattern matches the complete number otherwise false.
     */
    private static boolean isPatternMatched(final String completeNumber, final String pattern,
                                            final String prefixPattern) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "isPatternMatched start");
        boolean isPatternMatched = false;
        String singlePattern = null;
        //This logic will remove the 44 prefix from complete number so that we can check the dialled number pattern
        String numberWOPrefix = null;
        if (completeNumber.startsWith(prefixPattern)) {
            numberWOPrefix = completeNumber.substring(2, completeNumber.length());
        } else {
            numberWOPrefix = completeNumber;
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "pattern  = " + pattern);
        if (pattern.contains(BillBreakdownAPIConstants.COMMA)) {
            StringTokenizer eachPattern = new StringTokenizer(pattern, BillBreakdownAPIConstants.COMMA);
            while (eachPattern.hasMoreTokens()) {
                singlePattern = eachPattern.nextToken();
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "singlePattern = " + singlePattern);
                if (numberWOPrefix.startsWith(singlePattern)) {
                    isPatternMatched = true;
                    break; //NOPMD
                }
            }
        //For non comma seperated entries
        } else if (numberWOPrefix.startsWith(pattern)) {
            isPatternMatched = true;
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "isPatternMatched = " + isPatternMatched);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "isPatternMatched end");
        return isPatternMatched;
    }
    /**
     * This method will get the config file key from which the list of config values are supposed to be fetched based
     * on the tillUsageType passed as parameter to this method.
     * @param configMap
     *          Taskflow configuration file entries of format String, String.
     * @param tillUsageScenario
     *          Usage type got from TIL.
     * @param isRoaming
     *          true if complete number from TIL ir roaming number otherwise false.
     * @param isPrefix
     *          true if we need to get the key for 44 prefix check else false.
     * @return configKey
     *          The key using which the list of config file entries are supposed to be fetched.
     */
    private static String getConfigKeyForGIDUsageScenario(final Map<String, String> configMap,
                                                          final String tillUsageScenario, final boolean isRoaming,
                                                          final boolean isPrefix) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "getConfigKeyForSubscription start");
        String configKey = null;
        String voiceUsageType = configMap.get(BillBreakdownAPIConstants.VOICE_DETAIL_USAGE);
        String smUsageType = configMap.get(BillBreakdownAPIConstants.SMS_DETAIL_USAGE);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "voiceUsageType = " + voiceUsageType);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "smsUsageType = " + smUsageType);
        if (null != voiceUsageType && !voiceUsageType.isEmpty() && null != smUsageType
            && !smUsageType.isEmpty()) {
            if (tillUsageScenario.equalsIgnoreCase(voiceUsageType) && isRoaming) {
                if (isPrefix) {
                    configKey = BillBreakdownAPIConstants.VOICE_ROAMING_PREFIX;
                } else {
                    configKey = BillBreakdownAPIConstants.VOICE_ROAMING_LIST;
                }
            } else if (tillUsageScenario.equalsIgnoreCase(voiceUsageType) && !isRoaming) {
                if (isPrefix) {
                    configKey = BillBreakdownAPIConstants.VOICE_NONROAMING_PREFIX;
                } else {
                    configKey = BillBreakdownAPIConstants.VOICE_NONROAMING_LIST;
                }
            } else if (tillUsageScenario.equalsIgnoreCase(smUsageType) && isRoaming) {
                if (isPrefix) {
                    configKey = BillBreakdownAPIConstants.TEXT_ROAMING_PREFIX;
                } else {
                    configKey = BillBreakdownAPIConstants.TEXT_ROAMING_LIST;
                }
            } else if (tillUsageScenario.equalsIgnoreCase(smUsageType) && !isRoaming) {
                if (isPrefix) {
                    configKey = BillBreakdownAPIConstants.TEXT_NONROAMING_PREFIX;
                } else {
                    configKey = BillBreakdownAPIConstants.TEXT_NONROAMING_LIST;
                }
            }
        } else {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_ERROR, "voiceUsageType or smsUsageType from config file is "
                + "NULL/empty");
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "configKey = " + configKey);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "getConfigKeyForSubscription end");
        return configKey;
    }
    /**
     * This method is used to format the charged quantity received from GID for Data type into
     * volume which will be in MB format.
     * @param volume value returned by GIS as chargedquantity for Data type.
     * @return data formatted in MB.
     */
    public static final BigDecimal formatAountUserForDataUsage(final String volume) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "start formatAountUserForDataUsage");
        Double data;
        double chargedQuantity;
        if (null != volume) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "volume is" + volume);
            chargedQuantity = Double.parseDouble(volume);
            data = chargedQuantity / BillBreakdownAPIConstants.CONVERTION_FACTOR_DATA;
            return BillBreakdownAPIHelper.convertToTwoDecPlaces(data);
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                           "end formatAountUserForDataUsage");
        return null;
    }

    /**
     * This method is used to format the charged quantity received from GID for calls type into
     * duration which will be in minutes.
     * @param duration value returned as chargedquantity for voice type.
     * @return minute value in String format.
     */
    public static final String formatAmountUsedForVoiceUsageInMin(final String duration) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "start formatAmountUsedForVoiceUsageInMin");
        Integer minutes = 0;
        //Defect fix || CI 11 || 51302 || Start
        Double chargedQuantity;
        if (null != duration) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "duration value is" + duration);
            chargedQuantity = Double.parseDouble(duration);
            minutes = chargedQuantity.intValue() / BillBreakdownAPIConstants.CONVERTION_FACTOR_VOICE;
            //Defect fix || CI 11 || 51302 || End
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "minutes value of duration is" + minutes);
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                           "end formatAmountUsedForVoiceUsageInMin");
         return minutes.toString();
    }
    /**
     * This method is used to format the charged quantity received from GID for calls type into
     * duration which will be in Seconds.
     * @param duration value returned as chargedquantity for voice type.
     * @return seconds value in String format.
     */
    public static final String formatAmountUsedForVoiceUsageInSeconds(final String duration) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "start formatAmountUsedForVoiceUsageInSeconds");
        Integer seconds = 0;
        //Defect fix || CI 11 || 51302 || Start
        Double chargedQuantity;
        if (null != duration) {
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "duration is" + duration);
            chargedQuantity = Double.parseDouble(duration);
            seconds = chargedQuantity.intValue() % BillBreakdownAPIConstants.CONVERTION_FACTOR_VOICE;
            //Defect fix || CI 11 || 51302 || End
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "seconds value of duration is" + seconds);
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                           "end formatAmountUsedForVoiceUsageInSeconds");
         return seconds.toString();
    }
    /**
     * This method is used to format the fixed Line number to 44.
     * @param completeNumber String.
     * @return completeNumber value in 44 format.
     * Changes WR32847 || CI 11 || Start
     */
    public static final String changeToCTNFormat(final String completeNumber) {
        String completeNumberFormat = null;
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "start changeToCTNFormat");
        if (completeNumber.length() == (BillBreakdownAPIConstants.ELEVEN)
            && completeNumber.startsWith("0", 0)) {
            completeNumberFormat = completeNumber.replaceFirst("0", "44");
            }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                        "end changeToCTNFormat, completeNumber: " + completeNumberFormat);
         return completeNumberFormat;
    }
    //Code changes for WR33296 | Starts
    /**
     * This method is used to populate the GIPL response received for inactive subscription and create subsction
     * object from it and will do all the mapping related to bundleid, subsscriptiontype and associated accountids.
     * @param inactiveSubs list of all the inactive subscription.
     * @param installedProductResponse response received from GIPL.
     * @return map of serial number as key and subscription as value for all inactive subscription.
     */
    public static final Map<String, Subscription> populateGIPLResponseForInactiveSubs(final List<String> inactiveSubs,
                                                final GetInstalledProductListResponseDTO installedProductResponse) {
        Map<String, Subscription> inactiveSubsMap = null;
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO,
                           "inside populateGIPLResponseForInactiveSubs method ... ");
        QueryInstalledProductListResponseDTO queryResponseDTO = null;
        List<String> rootIdList = null;
        Subscription subscription = null;
        String promotionAssetID = null;
        String assetId = null;
        String rootAssetId = null;
        String productId = null;
        String parentAssetId = null;
        String serialNum = null;
        String alternateAssetId = null;
        String referenceValue = null;
        Map<String, String> configFile = null;
        Map<String, String> promotionIDMap = null;
        if (installedProductResponse != null && null != inactiveSubs) {
            promotionIDMap = new HashMap<String, String>();
            inactiveSubsMap = new HashMap<String, Subscription>();
            configFile = SessionContextSubscriptionDetailHelper.readSubscriptionTypeConfig();
            rootIdList = createRootIdList(configFile);
           for (QueryResponseDataAreaTypeDTO queryResponseDataDTO
                            : installedProductResponse.getQueryRespDataAreaTypeDTOList()) {
                queryResponseDTO = queryResponseDataDTO.getQueryResponseDTO();
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "iterating through gipl response");
            if (null != queryResponseDTO) {
               if (null != queryResponseDTO.getIdentificationDTO()
                    && null != queryResponseDTO.getIdentificationDTO().getAlternateObjectKeyDTO()
                    && null != queryResponseDTO.getIdentificationDTO().getAlternateObjectKeyDTO().getIdentity()) {
                   alternateAssetId = queryResponseDTO.getIdentificationDTO().getAlternateObjectKeyDTO().getIdentity();
                   VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Alternate Asset ID:" + alternateAssetId);
                }
               if (null != queryResponseDTO.getIdentificationDTO()
                    && (null != queryResponseDTO.getIdentificationDTO().getApplicationObjectDTO())) {
                    assetId = queryResponseDTO.getIdentificationDTO().getApplicationObjectDTO().getIdentity();
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "assetId---->" + assetId);
               }
               if (null != queryResponseDTO.getRelatedInstalledProductDTOList()) {
                   for (RelatedInstalledProductDTO dto : queryResponseDTO.getRelatedInstalledProductDTOList()) {
                       if (null != dto.getIdentificationDTO()
                           && null != dto.getIdentificationDTO().getApplicationObjectDTO()
                           && SubscriptionConstants.ROOT_RELATIONSHIP_CODE.getValue()
                           .equalsIgnoreCase(dto.getRelationshipCode())) {
                           rootAssetId = dto.getIdentificationDTO().getApplicationObjectDTO().getIdentity();
                           VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "rootAssetId---->" + rootAssetId);
                       }
                   }
               }
               if (null != queryResponseDTO.getItemReferenceDTO() && null != queryResponseDTO.getItemReferenceDTO()
                    .getItemIdentificationDTO() && (null != queryResponseDTO.getItemReferenceDTO()
                    .getItemIdentificationDTO().getApplicationObjtKeyDTO()) && (null != queryResponseDTO
                    .getItemReferenceDTO().getItemIdentificationDTO().getApplicationObjtKeyDTO().getIdentity())) {
                   productId = queryResponseDTO.getItemReferenceDTO().getItemIdentificationDTO()
                                   .getApplicationObjtKeyDTO().getIdentity();
                   VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "productId---->" + productId);
               }
               if (null != queryResponseDTO.getCompInstalledProductDTOList()
                   && (queryResponseDTO.getCompInstalledProductDTOList().size() > 0)
                   && (null != queryResponseDTO.getCompInstalledProductDTOList()
                       .get(0).getInstalledProductReferenceDTO())
                   && (null != queryResponseDTO.getCompInstalledProductDTOList().get(0)
                       .getInstalledProductReferenceDTO().getInstalledProductIdentificationDTO())
                   && (null != queryResponseDTO.getCompInstalledProductDTOList().get(0)
                       .getInstalledProductReferenceDTO().getInstalledProductIdentificationDTO()
                       .getApplicationObjectKeyDTO())
                   && (null != queryResponseDTO.getCompInstalledProductDTOList().get(0)
                       .getInstalledProductReferenceDTO().getInstalledProductIdentificationDTO()
                       .getApplicationObjectKeyDTO().getIdentity())) {
                   promotionAssetID = queryResponseDTO.getCompInstalledProductDTOList()
                                .get(0).getInstalledProductReferenceDTO().getInstalledProductIdentificationDTO()
                                .getApplicationObjectKeyDTO().getIdentity();
                   VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "promotionAssetID---->" + promotionAssetID);
               }
               if (null != queryResponseDTO.getSerialNumber()) {
                   serialNum = queryResponseDTO.getSerialNumber();
               }
               if (StringHelper.isEmpty(parentAssetId) && assetId != null && rootAssetId != null && assetId
                   .equalsIgnoreCase(rootAssetId) && StringUtil.isNotEmpty(promotionAssetID) && null != serialNum) {
                   if (null != rootIdList && !rootIdList.isEmpty()
                       && rootIdList.contains(productId) && inactiveSubs.contains(serialNum)) {
                       subscription = new Subscription();
                       subscription.setSerialNumber(StringUtil.formatCTNtoLocalCTN(serialNum));
                       VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Creating a subscription object "
                                                       + subscription.getSerialNumber());
                       subscription.setPromotionAssetId(promotionAssetID);
                       subscription.setSubscriptionType(setSubscriptionTypeFromConfig(productId, configFile));
                       VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Subscription Type-----> : for Subscription"
                           + subscription.getSerialNumber() + " is " + subscription.getSubscriptionType());
                       if (null != queryResponseDTO.getIdentificationDTO().getContextDTO() && StringUtil.isNotNull(
                            queryResponseDTO.getIdentificationDTO().getContextDTO().getSchemaID()) && queryResponseDTO
                           .getIdentificationDTO().getContextDTO().getSchemaID().equalsIgnoreCase("CUSTOMER_REFERENCE")
                           && StringUtil.isNotEmpty(queryResponseDTO.getIdentificationDTO()
                               .getContextDTO().getStringValue())) {
                           subscription.setReferenceName(queryResponseDTO.getIdentificationDTO()
                                                             .getContextDTO().getStringValue());
                       } else {
                           VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "ContextID not Present In GIPL");
                           referenceValue = getSubcriptionReferenceNameFromConfig(
                                                   subscription.getSubscriptionType(), configFile);
                           subscription.setReferenceName(referenceValue);
                       }
                       VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "RefrncNme" + subscription.getReferenceName());
                       VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, " subscription.serialNum() : "
                                   + subscription.getSerialNumber());
                       for (InstalledProductCustomerDTO instldPrdctCstmr
                            : queryResponseDTO.getInstalledProductCustomerDTOList()) {
                           subscription.setUsingAccountId(instldPrdctCstmr.getCustPartyRefDTO()
                                       .getCustPartyAcctIdentifiDTO().getApplicationObjectDTO().getIdentity());
                           VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Using Account Id for Subscription---->"
                               + subscription.getSerialNumber() + "  is-:: " + subscription.getUsingAccountId());
                       }
                        inactiveSubsMap.put(subscription.getSerialNumber(), subscription);
                    }
               } else if (StringHelper.isEmpty(parentAssetId) && StringUtil.isNotEmpty(assetId)
                        && StringUtil.isNotEmpty(rootAssetId) && StringUtil.isNotEmpty(alternateAssetId)
                        && assetId.equalsIgnoreCase(rootAssetId) && StringUtil.isEmpty(serialNum)
                        && (null == promotionAssetID || StringUtil.isEmpty(promotionAssetID))) {
                    VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "Creating Promotion MaP");
                    promotionIDMap.put(alternateAssetId, productId);
               }
           }
        }
           if (null != inactiveSubsMap && !inactiveSubsMap.isEmpty()
                            && null != promotionIDMap && !promotionIDMap.isEmpty()) {
             VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "SUBSCRIPTION LIST SIZE: " + inactiveSubsMap);
             VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG, "PROMOTION map: " + promotionIDMap);
             populatePromotionIDToSubscription(inactiveSubsMap, promotionIDMap);

           }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of populateGIPLResponseForInactiveSubs");
        return inactiveSubsMap;
    }
    /**
     * This method is used to Map the promotionid into the subscription object.
     * @param suscriptionMap list of inactive subscription.
     * @param promotionIDMap map of alternateAssetId and promotionID
     */
    private static void populatePromotionIDToSubscription(final Map<String, Subscription> suscriptionMap,
                                                          final Map<String, String> promotionIDMap) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of populatePromotionIDToSubscription");
        if (null != suscriptionMap && null != promotionIDMap) {
            for (Subscription subs : suscriptionMap.values()) {
                if (null != subs.getPromotionAssetId()
                    && null != promotionIDMap.get(subs.getPromotionAssetId())) {
                    subs.setBundleId(promotionIDMap.get(subs.getPromotionAssetId()));
                }
            }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of populatePromotionIDToSubscription");
    }
    /**
     * This method is used to set SubscriptionType.
     * @param productId String.
     * @param configFile map.
     * @return String.
     */
    private static String setSubscriptionTypeFromConfig(final String productId, final Map<String, String> configFile) {
        String subType = null;
        Map.Entry map = null;
        if (configFile != null) {
            Iterator iterator = configFile.entrySet().iterator();
            while (iterator.hasNext()) {
                map = (Map.Entry) iterator.next();
                if (map.getValue().toString().contains(productId)) {
                    subType = map.getKey().toString();
                }
            }
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                               "Map = " + configFile);
            VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                               "Subscription Type for Product Id ["
                               + productId + "] is::::" + subType);
        }
        return subType;
    }
    /**
     * Method to find the Reference Name from Config File, based on the subscriptionType.
     * @param subscriptionType String
     * @param configFile Map.
     * @return ReferenceName String
     */
    private static String getSubcriptionReferenceNameFromConfig(final String subscriptionType,
                                                                final Map<String, String> configFile) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO,
                          "Start of Method getSubcriptionReferenceNameFromConfig ");
        String referenceValue = null;
        if (null != configFile && !configFile.isEmpty()) {
            if (subscriptionType.equalsIgnoreCase(SesInitializationConstants.MBB)) {
            referenceValue = configFile.get(SesInitializationConstants.REFERENCE_NAME_MBB);
            }
            if (subscriptionType.equalsIgnoreCase(SesInitializationConstants.MOBILE)) {
            referenceValue = configFile.get(SesInitializationConstants.REFERENCE_NAME_MOBILE);
            }
            if (subscriptionType.equalsIgnoreCase(SesInitializationConstants.FIXED)) {
            referenceValue = configFile.get(SesInitializationConstants.REFERENCE_NAME_FIXED);
            }
            if (subscriptionType.equalsIgnoreCase(SesInitializationConstants.FIXED_LINE)) {
            referenceValue = configFile.get(SesInitializationConstants.REFERENCE_NAME_FIXEDLINE);
            }
            if (subscriptionType.equalsIgnoreCase(SesInitializationConstants.FIXED_BROADBAND)) {
            referenceValue = configFile.get(SesInitializationConstants.REFERENCE_NAME_FIXEDBROADBAND);
            }
            if (subscriptionType.equalsIgnoreCase(SesInitializationConstants.TV_SERVICE)) {
            referenceValue = configFile.get(SesInitializationConstants.REFERENCE_NAME_TVSERVICE);
            }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                               "referenceValue is: " + referenceValue);
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO,
                          "End of Method getSubcriptionReferenceNameFromConfig ");
        return referenceValue;
    }
    /**
     * This method will create the rootidlist from DF_SubscriptionTypeConfig file.
     * @param configFile Map.
     * @return list of rootid.
     */
    private static List<String> createRootIdList(final Map<String, String> configFile) {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "Start of createRootIdList ");
        List<String> rootIdList = null;
        if (null != configFile.values()) {
            rootIdList = new ArrayList<String>();
            for (String ids : configFile.values()) {
                if (ids.contains(",")) {
                    rootIdList.addAll(Arrays.asList(ids.split(",")));
                } else {
                    rootIdList.add(ids);
                }
            }
        }
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_INFO, "End of createRootIdList ");
        return rootIdList;
    }
    //Code changes for WR33296 | Ends
}
