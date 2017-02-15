package com.poc.vi.main;

import java.io.IOException;

import com.poc.vi.core.VoiceIt;

public class VoiceVerifier implements IVoiceVerifier {

	VoiceIt voiceIt = new VoiceIt("5e28b006380249df839b61ef9440c83c");
	
	

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		VoiceVerifier verifier = new VoiceVerifier();
		
		verifier.execute();

	}
	
	
	public void execute()
	{
//		createUser(EMAIL2, PWD2, FIRST_NAME, LAST_NAME);
		
//		getUser(EMAIL, PWD);
		
		//createEnrollment(EMAIL, PWD, "C:\\E\\POC\\Nuance\\voice\\voicePrint.wav", "");
		
//		createEnrollment(EMAIL, PWD, "C:\\E\\POC\\Nuance\\voice\\downloaded.wav", "");
		
//		createEnrollmentByWavURL(EMAIL, PWD, "https:\\cdn.fbsbx.com\\v\\t59.3654-21\\16652222_1629729893720652_7252759647524749312_n.aac\\audioclip-1486478184733-2962.aac?oh=eb8e3be6ae8ad90a66fe16e27e637ea0&oe=589CFE59");
		
//		deleteEnrollment(EMAIL, PWD, "916324");	
		
		
//		getEnrollments(EMAIL, PWD);
		
		authentication(EMAIL, PWD, "C:\\E\\POC\\Nuance\\voice\\authvoice.wav", "70", "en-GB");
	}
	
	
	public boolean createUser(String email, String password, String firstName,
			String lastName) {
		boolean userCreated = true;
		try {
			String response = voiceIt.createUser(email, password, firstName,
					lastName);
			System.out.println("createUser : " + response);
		} catch (IOException e) {
			userCreated = false;
			e.printStackTrace();
		}

		return userCreated;
	}
	
	
	public boolean deleteUser(String email, String password)
	{
		boolean deleteUser = true;
		try {
			String response = voiceIt.deleteUser(email, password);
			System.out.println("deleteUser : " + response);
			
		} catch (IOException e) {
			deleteUser = false;
			e.printStackTrace();
		}
		
		return deleteUser;
	}
	
	public String getUser(String email, String password)
	{
		String response = null;
		try {
			 response = voiceIt.getUser(email, password);
			System.out.println("getUser : " + response);
			
		} catch (IOException e) {
			//deleteUser = false;
			e.printStackTrace();
		}
		
		return response;
	}
	
	public String createEnrollment(String email, String password, 
			String wavFilePath, String contentLang)
 {
		String createEnrollment = null;
		try {
			createEnrollment = voiceIt.createEnrollment(email, password,
					wavFilePath, contentLang);
			System.out.println("createEnrollment : " + createEnrollment);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return createEnrollment;
	}
	
	public String createEnrollmentByWavURL(String email, String password, 
			String wavFileUrl)
 {
		String createEnrollmentByWavURL = null;
		try {
			createEnrollmentByWavURL = voiceIt.createEnrollmentByWavURL(email, password, wavFileUrl);
			System.out.println("createEnrollmentByWavURL : " + createEnrollmentByWavURL);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return createEnrollmentByWavURL;
	}
	
	
	public String deleteEnrollment(String email, String password, 
			String enrollmentID)
 {
		String deleteEnrollment = null;
		try {
			deleteEnrollment = voiceIt.deleteEnrollment(email, password, enrollmentID);
			System.out.println("deleteEnrollment : " + deleteEnrollment);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return deleteEnrollment;
	}
	
	
	public String getEnrollments(String email, String password)
 {
		String getEnrollments = null;
		try {
			getEnrollments = voiceIt.getEnrollments(email, password);
			System.out.println("getEnrollments : " + getEnrollments);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return getEnrollments;
	}
	
	public String authentication(String email, String password, 
			String wavFilePath, String confidenceLevel, String contentLang)
 {
		String authentication = null;
		try {
			authentication = voiceIt.authentication(email, password,
					wavFilePath, confidenceLevel, contentLang);
			System.out.println("authentication : " + authentication);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return authentication;
	}
	
}
