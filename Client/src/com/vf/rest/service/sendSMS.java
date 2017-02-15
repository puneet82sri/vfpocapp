package com.vf.rest.service;

import com.vodafone.online.eserv.api.dynamiclanding.GetBalanceAPI;
import com.vodafone.online.eserv.services.request.getbalance.GetBalanceRequestDTO;

import java.io.BufferedReader;
    import java.io.InputStreamReader;
    import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
//    import java.net.URLConnection;
//    import java.net.URLEncoder;
//     
    public class sendSMS {
    	public static  String sendSms(String ctn, String otac) {
    		try {
                    
    		    ctn=ctn.replaceFirst("^0+(?!$)", "");
    		    String msisdn = ctn;
                    
    			// Construct data
    			//String user = "username=" + "steve@blazingpath.com";
    			String hash = "apiKey=" + "elaq%2BwpFWc0-PbcE01PiY94mrg24QL0XLmf0u4NTZ7";
    			String message = "&message=" + "Your VF Billing Interactive Demo security code is "+otac+" Please tell to Echo device to complete your security detials";
    			String sender = "&sender=" + "VFAlexaDemo ";
    			String numbers = "&numbers=" + msisdn;
    			
    			// Send data
    		//	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("ddd.ccc.com", 8080));
    		//	HttpURLConnection conn = (HttpURLConnection) new URL("http://api.txtlocal.com/send/").openConnection(proxy);

    			HttpURLConnection conn = (HttpURLConnection) new URL("http://api.txtlocal.com/send/?").openConnection();
    			//String data = user + hash + numbers + message + sender;
    			String data =  hash + numbers + message + sender;
                    
                    System.out.println ("SMS details "+data);
    			conn.setDoOutput(true);
    			conn.setRequestMethod("POST");
    			conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
    			conn.getOutputStream().write(data.getBytes("UTF-8"));
    			final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    			final StringBuffer stringBuffer = new StringBuffer();
    			String line;
    			while ((line = rd.readLine()) != null) {
    				stringBuffer.append(line);
    			}
    			rd.close();
    			
    			return stringBuffer.toString();
    		} catch (Exception e) {
    			System.out.println("Error SMS "+e);
    			return "Error "+e;
    		}
    	}
        
        public static void main(String[] args) {
            
            
            
            
            System.out.println("response  :: " + sendSms("07502355070","1234"));
        }
    }