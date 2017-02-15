package com.vf.rest.service;

import com.clockworksms.*;

public class DemoSMS
{
    public DemoSMS() {
        
    
    }
   public static String sendSMS(String ctn, String otac)
   {
       
       String msg = "Your VF Billing Interactive Demo security code is "+otac+" Please tell to Echo device to complete your security detials";
       String apikey = "ebafbcbdae8e130d5be53b7ed633f0a95efea084";
       String result1= "";
      try
      {
         ClockWorkSmsService clockWorkSmsService = new ClockWorkSmsService(apikey);
          ctn=ctn.replaceFirst("^0+(?!$)", "");
          String msisdn = ctn;
          System.out.println ("msisdn --"+msisdn);
          
         SMS sms = new SMS("0044"+ctn, msg);
         ClockworkSmsResult result = clockWorkSmsService.send(sms);

         if(result.isSuccess())
         {
            System.out.println("Sent with ID: " + result.getId());
             result1 = "success";
         }
         else
         {
            System.out.println("Error: " + result.getErrorMessage());
             result1 = "failure";
         }
      }
      catch (ClockworkException e)
      {
         e.printStackTrace();
      }
      return result1;
   }
}