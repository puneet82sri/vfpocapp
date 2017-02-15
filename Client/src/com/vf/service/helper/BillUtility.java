package com.vf.service.helper;

import com.vodafone.online.eserv.api.ebillingv2.BillBreakdownAPI;
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

//import com.vodafone.online.eserv.schemas.datafile.ElementType;
//import com.vodafone.online.eserv.schemas.datafile.ListRowType;
//import com.vodafone.online.eserv.schemas.datafile.ListType;
//import com.vodafone.online.eserv.schemas.datafile.RootDocument;

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
//     @Before
//     public final void setUp1() throws Exception {
//         System.setProperty("jtc.services.file",
//                                    "C:\\Users\\natagand\\AppData"
//                                    + "\\Roaming\\JDeveloper\\system11.1.1.5.37.60.13\\DefaultDomain\\AllServices.xml");
//         System.setProperty("jtc.properties.file",
//                                    "C:\\Users\\natagand\\AppData"
//                                    + "\\Roaming\\JDeveloper\\system11.1.1.5.37.60.13\\DefaultDomain\\all.properties");
//     }
//    @Test
   
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
