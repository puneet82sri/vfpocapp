package com.poc.vi.core;


import com.sun.net.ssl.HostnameVerifier;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSession;

public class VoiceIt {

	String developerId;
	String platformId = "3";
	public VoiceIt(String developerId) {
		this.developerId = developerId;
	}

	private String GetSHA256(String data) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			sha.update(data.getBytes());
			byte[] hash = sha.digest();

			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	private String readInputStream(InputStream inputStream) throws IOException {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            
		
			int result = inputStream.read();
			while (result != -1) {
				outputStream.write((byte) result);
				result = inputStream.read();
			}
			return outputStream.toString();
		
	}

	public String getUser(String email, String password) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/users");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str = "";
		try {
			connection.setRequestMethod("GET");
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("PlatformID", platformId);
			// read response
                    InputStream inputStream =
                        connection.getResponseCode() == 200 ? connection.getInputStream() :
                        connection.getErrorStream();
                    str = readInputStream(inputStream);
                    } catch (IOException e) {

                    throw e;
                    } finally {
                    if (connection != null)
                        connection.disconnect();
                    }

                    return str;
	}

	public String createUser(String email, String password, String firstName,
                             String lastName, String phone1, String phone2,
                             String phone3) throws IOException {
        URL url =
            new URL("https://siv.voiceprintportal.com/sivservice/api/users");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        String str = "";
        try {
            connection.setRequestMethod("POST");
            connection.addRequestProperty("VsitEmail", email);
            connection.addRequestProperty("VsitPassword", GetSHA256(password));
            connection.addRequestProperty("VsitDeveloperId", developerId);
            connection.addRequestProperty("VsitFirstName", firstName);
            connection.addRequestProperty("VsitLastName", lastName);
            connection.addRequestProperty("VsitPhone1", phone1);
            connection.addRequestProperty("VsitPhone2", phone2);
            connection.addRequestProperty("VsitPhone3", phone3);
            connection.addRequestProperty("PlatformID", platformId);

            // read response


            InputStream inputStream =
                connection.getResponseCode() == 200 ? connection.getInputStream() :
                connection.getErrorStream();
            str = readInputStream(inputStream);
        } catch (IOException e) {

            throw e;
        } finally {
            if (connection != null)
                connection.disconnect();
        }

        return str;
    }

	//Added CreateUser Method Calls to make the phone numbers optional
	public String createUser(String email, String password,String firstName,String lastName) throws IOException
	{
		return createUser(email,password,firstName,lastName,"","","");
	}

	public String createUser(String email, String password,String firstName,String lastName,String phone1) throws IOException
	{
		return createUser(email,password,firstName,lastName,phone1,"","");
	}

	public String createUser(String email, String password,String firstName,String lastName,String phone1,String phone2) throws IOException
	{
		return createUser(email,password,firstName,lastName,phone1,phone2,"");
	}


	public String setUser(String email, String password,String firstName,String lastName, String phone1,String phone2,String phone3) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/users");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str="";
		try {
			connection.setRequestMethod("PUT");
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("VsitFirstName", firstName);
			connection.addRequestProperty("VsitLastName", lastName);
			connection.addRequestProperty("VsitPhone1", phone1);
			connection.addRequestProperty("VsitPhone2", phone2);
			connection.addRequestProperty("VsitPhone3", phone3);
			connection.addRequestProperty("PlatformID", platformId);

			    InputStream inputStream =
			        connection.getResponseCode() == 200 ? connection.getInputStream() :
			        connection.getErrorStream();
			    str = readInputStream(inputStream);
			    } catch (IOException e) {

			    throw e;
			    } finally {
			    if (connection != null)
			        connection.disconnect();
			    }

			    return str;
	}

	public String setUser(String email, String password,String firstName,String lastName) throws IOException
	{
		return setUser(email,password,firstName,lastName,"","","");
	}

	public String setUser(String email, String password,String firstName,String lastName,String phone1) throws IOException
	{
		return setUser(email,password,firstName,lastName,phone1,"","");
	}

	public String setUser(String email, String password,String firstName,String lastName,String phone1,String phone2) throws IOException
	{
		return setUser(email,password,firstName,lastName,phone1,phone2,"");
	}

	public String deleteUser(String email, String password) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/users");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str = "";
		try {
			connection.setRequestMethod("DELETE");
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("PlatformID", platformId);

			// read response
			    InputStream inputStream =
			        connection.getResponseCode() == 200 ? connection.getInputStream() :
			        connection.getErrorStream();
			    str = readInputStream(inputStream);
			    } catch (IOException e) {

			    throw e;
			    } finally {
			    if (connection != null)
			        connection.disconnect();
			    }

			    return str;
	}

	public String createEnrollment(String email, String password,String pathToEnrollmentWav,String contentLanguage) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/enrollments");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str="";
//		byte [] myData = Files.readAllBytes(Paths.get(pathToEnrollmentWav));
                byte [] myData = readFileAsBytes(pathToEnrollmentWav);
//                new OutputStream();
		try {
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.addRequestProperty("PlatformID", platformId);
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("ContentLanguage", contentLanguage);
			connection.setRequestProperty("Content-Type","audio/wav");
			DataOutputStream request = new DataOutputStream(connection.getOutputStream());
			request.write(myData);
			request.flush();
			request.close();
			// read response
			    InputStream inputStream =
			        connection.getResponseCode() == 200 ? connection.getInputStream() :
			        connection.getErrorStream();
			    str = readInputStream(inputStream);
			    } catch (IOException e) {

			    throw e;
			    } finally {
			    if (connection != null)
			        connection.disconnect();
			    }

			    return str;
	}

	public String createEnrollment(String email, String password,String pathToEnrollmentWav)throws IOException{
		try{
			return createEnrollment(email,password,pathToEnrollmentWav,"");
		}
		catch(IOException e){
			return "Failed: IOException";
		}
	}

	public String createEnrollmentByWavURL(String email, String password,String urlToEnrollmentWav,String contentLanguage) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/enrollments/bywavurl");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str = "";
		try {
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("VsitwavURL", urlToEnrollmentWav);
			connection.addRequestProperty("ContentLanguage", contentLanguage);
			connection.addRequestProperty("PlatformID", platformId);
			connection.setRequestProperty("Content-Type","audio/wav");

			InputStream inputStream =
			    connection.getResponseCode() == 200 ? connection.getInputStream() :
			    connection.getErrorStream();
			str = readInputStream(inputStream);
			} catch (IOException e) {

			throw e;
			} finally {
			if (connection != null)
			    connection.disconnect();
			}

			return str;
	}

	public String createEnrollmentByWavURL(String email, String password,String urlToEnrollmentWav)throws IOException{
		try{
			return createEnrollmentByWavURL(email,password,urlToEnrollmentWav,"");
		}
		catch(IOException e){
			return "Failed: IOException";
		}
	}

	public String deleteEnrollment(String email, String password,String enrollmentId) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/enrollments"+"/"+enrollmentId);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str = "";
		try {
			connection.setRequestMethod("DELETE");
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("PlatformID", platformId);

			// read response
			    InputStream inputStream =
			        connection.getResponseCode() == 200 ? connection.getInputStream() :
			        connection.getErrorStream();
			    str = readInputStream(inputStream);
			    } catch (IOException e) {

			    throw e;
			    } finally {
			    if (connection != null)
			        connection.disconnect();
			    }

			    return str;
	}

	public String getEnrollments(String email, String password) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/enrollments");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str= "";
		try {
			connection.setRequestMethod("GET");
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("PlatformID", platformId);

			// read response
			    InputStream inputStream =
			        connection.getResponseCode() == 200 ? connection.getInputStream() :
			        connection.getErrorStream();
			    str = readInputStream(inputStream);
			    } catch (IOException e) {

			    throw e;
			    } finally {
			    if (connection != null)
			        connection.disconnect();
			    }

			    return str;
	}

	public String authentication(String email, String password,String pathToAuthenticationWav, String confidence, String contentLanguage) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/authentications");
//		byte [] myData = Files.readAllBytes(Paths.get(pathToAuthenticationWav));
                byte [] myData = readFileAsBytes(pathToAuthenticationWav);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str="";
		try {
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("VsitConfidence", confidence);
			connection.addRequestProperty("ContentLanguage", contentLanguage);
			connection.addRequestProperty("PlatformID", platformId);
			connection.setRequestProperty("Content-Type","audio/wav");
//                    connection
//			    connection.setHostnameVerifier(new HostnameVerifier()  
//			                {        
//			                    public boolean verify(String hostname, SSLSession session)  
//			                    {  
//			                        return true;  
//			                    }  
//			                }); 




			DataOutputStream request = new DataOutputStream(connection.getOutputStream());
			request.write(myData);
			request.flush();
			request.close();

			InputStream inputStream =
			    connection.getResponseCode() == 200 ? connection.getInputStream() :
			    connection.getErrorStream();
			str = readInputStream(inputStream);
			} catch (IOException e) {

			throw e;
			} finally {
			if (connection != null)
			    connection.disconnect();
			}

			return str;
	}

	public String authentication(String email, String password, String pathToAuthenticationWav, String confidence)throws IOException{
		try{
			return authentication(email, password, pathToAuthenticationWav, confidence,"");
		}
		catch(IOException e){
			return "Failed: IOException";
		}
	}

	public String authenticationByWavURL(String email, String password, String urlToAuthenticationWav, String confidence, String contentLanguage) throws IOException {
		URL url = new URL("https://siv.voiceprintportal.com/sivservice/api/authentications/bywavurl");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String str = "";
		try {
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.addRequestProperty("VsitEmail", email);
			connection.addRequestProperty("VsitPassword", GetSHA256(password));
			connection.addRequestProperty("VsitDeveloperId", developerId);
			connection.addRequestProperty("VsitConfidence", confidence);
			connection.addRequestProperty("VsitwavURL", urlToAuthenticationWav);
			connection.addRequestProperty("ContentLanguage", contentLanguage);
			connection.addRequestProperty("PlatformID", platformId);
			connection.setRequestProperty("Content-Type","audio/wav");

			InputStream inputStream =
			    connection.getResponseCode() == 200 ? connection.getInputStream() :
			    connection.getErrorStream();
			str = readInputStream(inputStream);
			} catch (IOException e) {

			throw e;
			} finally {
			if (connection != null)
			    connection.disconnect();
			}

			return str;
	}

	public String authenticationByWavURL(String email, String password, String urlToAuthenticationWav, String confidence)throws IOException{
		try{
			return authenticationByWavURL(email, password, urlToAuthenticationWav, confidence,"");
		}
		catch(IOException e){
			return "Failed: IOException";
		}
	}
        
        
        
        private byte[] readFileAsBytes(String filePath) {
            File file = new File(filePath);
            //init array with file length
            byte[] bytesArray = new byte[(int) file.length()];

            FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray); //read file into bytes[]
            if (fis != null) {
                fis.close();
            }
            
        } catch (Exception e) {
        }
            return bytesArray;
        }


}
