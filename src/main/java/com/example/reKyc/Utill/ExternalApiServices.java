package com.example.reKyc.Utill;


import com.example.reKyc.Model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExternalApiServices {

    @Value("${singzy.fileExchange}")
    private String fileExchangeBase64Url;

    @Value("${singzy.ttl}")
    private String fileExchangeBase64Ttl;

    @Value("${singzy.extraction.aadhar}")
    private String extractAadharUrl;
    @Value("${singzy.extraction.pan}")
    private String extractPanUrl;

    @Value("${singzy.authorisation.key}")
    private String singzyAuthKey;

    @Value("${singzy.send.otp.aadhar}")
    private String sendOtpAadharUrl;
    @Value("${singzy.verify.otp.aadhar}")
    private String verifyOtpAadharUrl;

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();

    public HashMap convertBase64ToUrl(String documentType, String base64String) {


        HashMap<String, String> inputBody = new HashMap<>();
        inputBody.put("base64String", base64String);
        inputBody.put("mimetype", documentType);
        inputBody.put("ttl", fileExchangeBase64Ttl);
        inputBody.put("sizw", "20MB");
        HashMap<String, String> urlResponse = new HashMap<>();
        ResponseOfBase64 responseOfBase64 = new ResponseOfBase64();

        try {


            responseOfBase64 = restTemplate.postForObject(fileExchangeBase64Url, inputBody, ResponseOfBase64.class);
            System.out.println(responseOfBase64.getFile().directURL);
            String url = responseOfBase64.getFile().directURL;

            if (responseOfBase64 != null) {
                urlResponse.put("fileUrl", url);
            } else {
                urlResponse.put("code", "1111");
                urlResponse.put("msg", "Technical issue, please try again");

            }


        } catch (Exception e) {

            System.out.println(e);
            urlResponse.put("code", "1111");
            urlResponse.put("msg", "Technical issue, please try again");
        }
//urlResponse.get("file");

        return urlResponse;
    }

    public HashMap extractAadharDetails(List<String> urls) {

        HashMap<String, List> inputBody = new HashMap<>();
        inputBody.put("files", urls);
        HashMap<String, String> addressPreview = new HashMap<>();
        AadharResponse aadharResponse = new AadharResponse();
        try {

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", singzyAuthKey);
            HttpEntity<Map<String, List>> requestEntity = new HttpEntity<>(inputBody, headers);

            aadharResponse = restTemplate.postForObject(extractAadharUrl, requestEntity, AadharResponse.class);

            if (!(aadharResponse.getResult().getUid().isBlank()) && !(aadharResponse.getResult().getName().isBlank()) && !(aadharResponse.getResult().getAddress().isBlank()) && aadharResponse.getResult().isValidBackAndFront()) {

                System.out.println(aadharResponse);
                addressPreview.put("code", "0000");
                addressPreview.put("msg", "File extracted successfully");
                addressPreview.put("name", aadharResponse.getResult().getName());
                addressPreview.put("address", aadharResponse.getResult().getAddress());
                addressPreview.put("dateOfBirth", aadharResponse.getResult().getDateOfBirth());
                addressPreview.put("uid", aadharResponse.getResult().getUid());


            } else {
                addressPreview.put("code", "1111");
                addressPreview.put("msg", "File did not extracted, please try again");
            }
        } catch (Exception e) {
            System.out.println(e);
            addressPreview.put("code", "1111");
            addressPreview.put("msg", "Technical issue, please try again");
        }
        return addressPreview;
    }

    public HashMap extractPanDetails(List<String> urls) {

        HashMap<String, Object> inputBody = new HashMap<>();
        inputBody.put("files", urls);
        inputBody.put("type", "individualPan");
        inputBody.put("getRelativeData", true);
        HashMap<String, String> panResponse = new HashMap<>();

        try {

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", singzyAuthKey);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(inputBody, headers);
            System.out.println("request" + inputBody);
            ResponseEntity<PanCardResponse> extractPanResponse = restTemplate.postForEntity(extractPanUrl, requestEntity, PanCardResponse.class);

            if (extractPanResponse.getStatusCode().toString().contains("200")) {

                System.out.println("Response-" + extractPanResponse.getBody().getResult());
                System.out.println("staus" + extractPanResponse.getStatusCode());

                panResponse.put("code", "0000");
                panResponse.put("msg", "File extracted successfully");
                panResponse.put("name", extractPanResponse.getBody().getResult().getName());
//          panResponse.put("address", aadharResponse.getResult().getAddress());
                panResponse.put("dateOfBirth", extractPanResponse.getBody().getResult().getDob());
                panResponse.put("uid", extractPanResponse.getBody().getResult().getNumber());
            }

        } catch (Exception e) {
            System.out.println(e);
            panResponse.put("code", "1111");
            panResponse.put("msg", "Technical issue, please try again");
        }
        return panResponse;
    }


}
