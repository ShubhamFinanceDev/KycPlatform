package com.example.reKyc.Utill;


import com.example.reKyc.Model.AadharOtpResponse;
import com.example.reKyc.Model.AadharOtpVerifyResonse;
import com.example.reKyc.Model.AadharResponse;
import com.example.reKyc.Model.ResponseOfBase64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
//            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", singzyAuthKey);
            HttpEntity<Map<String, List>> requestEntity = new HttpEntity<>(inputBody, headers);

            aadharResponse = restTemplate.postForObject(extractAadharUrl, requestEntity, AadharResponse.class);

            if (!(aadharResponse.getResult().getUid().isBlank())&& !(aadharResponse.getResult().getName().isBlank()) && !(aadharResponse.getResult().getAddress().isBlank()) && aadharResponse.getResult().isValidBackAndFront()) {

                System.out.println(aadharResponse);
                addressPreview.put("code", "0000");
                addressPreview.put("msg", "File extracted successfully");
                addressPreview.put("name", aadharResponse.getResult().getName());
                addressPreview.put("address", aadharResponse.getResult().getAddress());
                addressPreview.put("dateOfBirth", aadharResponse.getResult().getDateOfBirth());
                addressPreview.put("uid",aadharResponse.getResult().getUid());


            }
            else
            {
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




    public HashMap<String,String> sendOtpOnLinkMobileNO(String aadharNo) {
        HashMap<String,String> inputBody=new HashMap<>();
        inputBody.put("aadhaarNumber",aadharNo);
        HashMap<String,String> otpDetails=new HashMap<>();

        try {
//            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", singzyAuthKey);
            HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(inputBody, headers);

            AadharOtpResponse responseSendOtp = restTemplate.postForObject(sendOtpAadharUrl, requestEntity, AadharOtpResponse.class);

            if (responseSendOtp.getStatusCode() == 200) {

                otpDetails.put("requestCode",responseSendOtp.getData().getRequestId());
                otpDetails.put("msg","otp send on registered mobile no");
                otpDetails.put("code","0000");
            }
            else
            {
                otpDetails.put("code", "1111");
                otpDetails.put("msg", "technical issue ,please try again");
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
            otpDetails.put("code", "1111");
            otpDetails.put("msg", "technical issue ,please try again");
        }
        return otpDetails;
    }

    public HashMap<String, String> validateOtp(String requestID, String otpCode) {
        HashMap<String,String> inputBody=new HashMap<>();
        inputBody.put("requestId",requestID);
        inputBody.put("otp",otpCode);

        HashMap<String,String> verifyDetails=new HashMap<>();

        try {

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", singzyAuthKey);
            HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(inputBody, headers);

            AadharOtpVerifyResonse responseVerifyOtp = restTemplate.postForObject(verifyOtpAadharUrl, requestEntity, AadharOtpVerifyResonse.class);

            if (responseVerifyOtp.getStatusCode() == 200) {

                String address=responseVerifyOtp.getData().getAddress().getLandmark()+" "+responseVerifyOtp.getData().getAddress().getHouse()+" "+responseVerifyOtp.getData().getAddress().getStreet()+" "+responseVerifyOtp.getData().getAddress().getVtc()+" "+responseVerifyOtp.getData().getAddress().getPo()+" "+responseVerifyOtp.getData().getAddress().getLoc()+" "+responseVerifyOtp.getData().getAddress().getSubdist()+" "+responseVerifyOtp.getData().getAddress().getDist()+","+responseVerifyOtp.getData().getAddress().getState()+"-"+responseVerifyOtp.getData().getZip();

               verifyDetails.put("name",responseVerifyOtp.getData().getFull_name());
               verifyDetails.put("uidNo",responseVerifyOtp.getData().getAadhaar_number());
                verifyDetails.put("address",address);
                verifyDetails.put("msg","Verification successful");
                verifyDetails.put("code","0000");
            }
            else
            {
                verifyDetails.put("code", "1111");
                verifyDetails.put("msg", "Otp is not valid.");
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
            verifyDetails.put("code", "1111");
            verifyDetails.put("msg", "Technical issue ,please try again");
        }
        return verifyDetails;
    }

    }
