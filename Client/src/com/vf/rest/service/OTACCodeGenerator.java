package com.vf.rest.service;


import com.vodafone.online.eserv.framework.utility.logger.VodafoneLogger;

public class OTACCodeGenerator {
    public OTACCodeGenerator() {
        super();
    }
    
    /**.
     *  This is a method, it generates OTAC code.
     *  @return OTAC code value
     **/
    public static String generateOTACCode() {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                           "generateOTACCode() method of OTACCodeGenerator");
        //  CR 566 and CR627 code merge start
                //Start CR 595 use of Random
                //L3 Fix | CR583 | Start
                String otacCode = com.vodafone.online.eserv.framework.utility.common
                .OTACCodeGenerator.generateOTACCode("4", null);
        //L3 Fix | CR583 | End
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
            "generateOTACCode() method of OTACCodeGenerator - OTAC Value :: " + otacCode);
        return otacCode;
        //End CR 595 use of Random
                //  CR 566 and CR627 code merge end
    }
    /**.
     *  This is a method, it generates OTAC code.
     *  @return OTAC code value
     **/
    public static String generateNumericOTACCode() {
        VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
                           "generateNumericOTACCode() method of OTACCodeGenerator");
        //  CR 566 and CR627 code merge start
                //Start CR595 Use of Random
                //L3 Fix | CR583 | Start
                String otacCode = com.vodafone.online.eserv.framework.utility.common
            .OTACCodeGenerator.generateOTACCode("4", null);
        //L3 Fix | CR583 | End
                VodafoneLogger.log(VodafoneLogger.LOGLEVEL_DEBUG,
            "generateOTACCode() method of generateNumericOTACCode - OTAC Value :: " + otacCode);
        return otacCode;
        // End CR595 Use of Random
                //  CR 566 and CR627 code merge end
    }
}
