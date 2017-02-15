package com.vf.rest.service;

import com.vodafone.online.eserv.constants.BillBreakdownAPIConstants;
import com.vodafone.online.eserv.ebillingv2.AccountDetails;

import com.vodafone.online.eserv.ebillingv2.BundleDetails;
import com.vodafone.online.eserv.ebillingv2.InvoiceSummarySubscriptions;
import com.vodafone.online.eserv.ebillingv2.UsageDetails;
import com.vodafone.online.eserv.exceptions.ProductSchemaException;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapBusinessException;
import com.vodafone.online.eserv.framework.jtc.exceptions.WrapSystemException;

import com.vodafone.online.eserv.framework.utility.logger.VodafoneLogger;
import com.vodafone.online.eserv.framework.utility.resource.constants.ResourceConstants;
import com.vodafone.online.eserv.framework.utility.resource.exception.ResourceLoadException;
import com.vodafone.online.eserv.framework.utility.resource.impl.ResourceLoaderSingleton;
import com.vodafone.online.eserv.framework.utility.xmlparser.XMLMessageProcessor;
import com.vodafone.online.eserv.framework.utility.xmlparser.XMLMessageProcessorInterface;
import com.vodafone.online.eserv.framework.utility.xmlparser.exception.VFXmlException;
import com.vodafone.online.eserv.helper.ebillingv2.BillBreakdownAPIHelper;

import com.vodafone.online.eserv.schemas.datafile.ElementType;
import com.vodafone.online.eserv.schemas.datafile.ListRowType;
import com.vodafone.online.eserv.schemas.datafile.ListType;
import com.vodafone.online.eserv.schemas.datafile.RootDocument;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.math.BigDecimal;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.xmlbeans.XmlObject;
//
//import org.junit.Before;
//import org.junit.Test;

public class BillUtility {
    public BillUtility() {
        super();
    }

    public final void testCsoApi() {
        System.out.println("Me");
    }
 
     public final void setUp1() throws Exception {
         System.setProperty("jtc.services.file",
                                    "C:\\Users\\natagand\\AppData"
                                    + "\\Roaming\\JDeveloper\\system11.1.1.5.37.60.13\\DefaultDomain\\AllServices.xml");
         System.setProperty("jtc.properties.file",
                                    "C:\\Users\\natagand\\AppData"
                                    + "\\Roaming\\JDeveloper\\system11.1.1.5.37.60.13\\DefaultDomain\\all.properties");
     }

    public final String testFetchBillSymmaryAndAccDetails() {
        String serviceAccountID = "";
        String billingProfileID = "";
        String invoiceID = "";
        AccountDetails currentAcDetails;
        AccountDetails previoustAcDetails;
        InvoiceSummarySubscriptions currentSummarySubscription =
            new InvoiceSummarySubscriptions();
        InvoiceSummarySubscriptions prevSummarySubscription =
            new InvoiceSummarySubscriptions();
        String currentSubscription = null;
        List<String> curentSubscriptionsList = new ArrayList<String>();
        String prevSubscription = null;
        List<String> prevSubscriptionsList = new ArrayList<String>();
        BigDecimal subscriptonCharge = null;
        BigDecimal subscriptonBundleCharge = null;
        BigDecimal subscriptonTotalCharge = null;
        String message =  "The monthly bill has increased due to following reasons that is  ";

        try {

            serviceAccountID = "315607033";
            billingProfileID = "1-1I0YE-2376";
            invoiceID = "0.0.0.10 /bill 440535754555 0";

            currentAcDetails =
                    BillBreakdownAPI.getBillSummary(serviceAccountID,
                                                    billingProfileID,
                                                    invoiceID);

            System.out.println("currentAcDetails " + currentAcDetails);


            serviceAccountID = "315607033";
            billingProfileID = "1-1I0YE-2376";
            invoiceID = "0.0.0.10 /bill 440535754564 0";

            previoustAcDetails =
                    BillBreakdownAPI.getBillSummary(serviceAccountID,
                                                    billingProfileID,
                                                    invoiceID);

            System.out.println("previoustAcDetails " + previoustAcDetails);

           

            if (currentAcDetails != null && previoustAcDetails != null) {

                if (null !=
                    currentAcDetails.getCurrentInvoiceSubscriptionSummary() &&
                    null !=
                    previoustAcDetails.getCurrentInvoiceSubscriptionSummary()) {

                    for (Map.Entry<String, InvoiceSummarySubscriptions> currentInvoiceSubscription :
                         currentAcDetails.getCurrentInvoiceSubscriptionSummary().entrySet()) {

                        for (Map.Entry<String, InvoiceSummarySubscriptions> prevInvoiceSubscription :
                             previoustAcDetails.getCurrentInvoiceSubscriptionSummary().entrySet()) {


                            if (null !=
                                currentInvoiceSubscription.getValue() &&
                                null != currentInvoiceSubscription.getKey() &&
                                null != prevInvoiceSubscription.getValue() &&
                                null != prevInvoiceSubscription.getKey()) {

                                //Current Invoice
                                currentSummarySubscription =
                                        currentInvoiceSubscription.getValue();
                                currentSubscription =
                                        currentSummarySubscription.getSubscriptionID();
                                System.out.println("currentSubscription" +
                                                   currentSubscription);
                                curentSubscriptionsList.add(currentSubscription);

                                // Prevoious Invoice
                                prevSummarySubscription =
                                        prevInvoiceSubscription.getValue();
                                prevSubscription =
                                        prevSummarySubscription.getSubscriptionID();
                                System.out.println("prevSubscription " +
                                                   prevSubscription);
                                prevSubscriptionsList.add(prevSubscription);
                                // Compare the cycle forwatd and data
                                if (currentSubscription.equalsIgnoreCase(prevSubscription)) {
                                    System.out.println("getCost  " + prevSummarySubscription.getInvoiceType().getCost());
                                    System.out.println("getItemChargeType  " + prevSummarySubscription.getInvoiceType().getItemChargeType());
                                    System.out.println("C getCost  " + currentSummarySubscription.getInvoiceType().getCost());
                                    System.out.println("C getItemChargeType  " + currentSummarySubscription.getInvoiceType().getItemChargeType());
                                    if (null != currentSummarySubscription && null != currentSummarySubscription.getInvoiceType()
                                        && null != currentSummarySubscription.getInvoiceType().getCost()
                                        && null != prevSummarySubscription && null != prevSummarySubscription.getInvoiceType()
                                    && null != prevSummarySubscription.getInvoiceType().getCost()) {
                                            if (prevSummarySubscription.getInvoiceType().getItemChargeType().equalsIgnoreCase(BillBreakdownAPIConstants.CYCLE_FORWARD)) {
                                                if (currentSummarySubscription.getInvoiceType().getCost().compareTo(prevSummarySubscription.getInvoiceType().getCost()) == 1) {

                                                    message = message + 
                                                            "  migration or upgrade from £ " +
                                                            prevSummarySubscription.getInvoiceType().getCost() +
                                                            " to £ " +
                                                            currentSummarySubscription.getInvoiceType().getCost() + " , ";
//                                                            " which  result in difference of " +
//                                                            (currentSummarySubscription.getInvoiceType().getCost().subtract(prevSummarySubscription.getInvoiceType().getCost()).toString());
                                                }
                                            }
                                       // System.out.println("First Message " + message);
                                        }
                                    for (UsageDetails curerentUsageDetails :
                                         currentSummarySubscription.getExtraUsageDetails()) {

                                        for (UsageDetails prevUsageDetails :
                                             prevSummarySubscription.getExtraUsageDetails()) {

                                            if (null != curerentUsageDetails &&
                                                null != prevUsageDetails) {

                                                if (curerentUsageDetails.getSubscriptionTypeCode().equalsIgnoreCase(prevUsageDetails.getSubscriptionTypeCode())) {

                                                    if (curerentUsageDetails.getSubscriptionTypeCode().equalsIgnoreCase("Data-Usage"))

                                                        if (curerentUsageDetails.getExtraUsageCharge().compareTo(prevUsageDetails.getExtraUsageCharge()) ==
                                                            1) {
                                                            
                                                            System.out.println ("Data usage -- ");

                                                            message =
                                                                    message + " and the  data usage from £ " +
                                                                    prevUsageDetails.getExtraUsageCharge() +
                                                                    " to £ " +
                                                                    curerentUsageDetails.getExtraUsageCharge() + " , " ;
//                                                                    " which result in  difference of  " +
//                                                                    (curerentUsageDetails.getExtraUsageCharge().subtract(prevUsageDetails.getExtraUsageCharge()).toString());
                                                        }
                                                }


                                            }
                                        }
                                    }
                                }
                            }

                            System.out.println("Second Message " + message);


                        }
                    }
                    if (curentSubscriptionsList != null &&
                        prevSubscriptionsList != null) {
                        removeDuplicateCTN(curentSubscriptionsList);
                        removeDuplicateCTN(prevSubscriptionsList);
                        System.out.println("curentSubscriptionsList " + curentSubscriptionsList.size());
                        System.out.println("prevSubscriptionsList " + prevSubscriptionsList.size());
                        if (curentSubscriptionsList.size() >
                            prevSubscriptionsList.size()) {

                            for (String currentSub :
                                 curentSubscriptionsList) {

                                for (String prevSub :
                                     prevSubscriptionsList) {

                                    if (currentSub.equalsIgnoreCase(prevSub)) {
                                        continue;
                                    } else {
                                        message =
                                                " " + message + " , and new line has been added 0" +
                                                prevSub;
                                    }


                                }

                            }
                        }

                    }
                }
            }

            System.out.println("Final Message " + message);
        }

        catch (WrapSystemException e) {
            System.out.println("WrapSystemException" + e.getMessage());
        } catch (WrapBusinessException e) {
            System.out.println("WrapBusinessException" + e.getMessage());
        } catch (ProductSchemaException e) {
            System.out.println("ProductSchemaException" + e.getMessage());
        }
        
        return message;

    }
    private void removeDuplicateCTN(final List<String> subsList) {
        for (int i = 1; i < subsList.size(); i++) {
            String a1 = subsList.get(i);
            String a2 = subsList.get(i-1);
            if (a1.equals(a2)) {
                subsList.remove(a1);
            }
        }
    }


}
