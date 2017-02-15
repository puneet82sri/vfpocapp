package com.vf.rest.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
 
// Simple send SMS programm
public class TextMarketerSendSMS {
    public static String sendSMS(String username, String password, String to, String message, String originator) {
        String url;
        StringBuilder inBuffer = new StringBuilder();
        try {
            url = "http://api.textmarketer.co.uk/gateway/" +
                "?username=" + username + "&password=" + password + "&option=xml" +
                "&to=" + to + "&message=" + URLEncoder.encode(message, "UTF-8") +
                "&orig=" + URLEncoder.encode(originator, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        try {
            URL tmUrl = new URL(url);
            URLConnection tmConnection = tmUrl.openConnection();
            tmConnection.setDoInput(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(tmConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                inBuffer.append(inputLine);
            in.close();
        } catch (IOException e) {
            return null;
        }
        return inBuffer.toString();
    }
    public static void main(String[] args) {
        // Example of use
        String response = sendSMS("myUsername", "myPassword", "4477777777", "My test message", "TextMessage");
        System.out.println(response);
    }
}