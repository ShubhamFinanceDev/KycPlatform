package com.example.reKyc.Utill;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class OfflineAadhaarUtility {
    @Value("${singzy.send.otp.aadhar}")
    private String getOkycOtpUrl;
    @Value("${singzy.verify.otp.aadhar}")
    private String fetchOkycDataUrl;
    @Value("${singzy.authorisation.key}")
    private String authorizationToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> requestIdUtility(String aadhaarNumber) {
        Map<String, Object> finalResponse = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("aadhaarNumber", aadhaarNumber);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity response = restTemplate.exchange(getOkycOtpUrl, HttpMethod.POST, entity, Map.class);
            Map<String, Object> puttingResponse = (Map<String, Object>) response.getBody();
            System.out.println("Response Body: " + puttingResponse);

            if (puttingResponse != null && puttingResponse.get("data") instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) puttingResponse.get("data");
                Object requestId = data.get("requestId");
                finalResponse.put("requestId", requestId);
            } else {
                throw new IllegalArgumentException("Unexpected response structure: " + puttingResponse);
            }

            finalResponse.put("response", response);
        } catch (Exception e) {
            finalResponse.put("Msg", "Exception found: " + e.getMessage());
            finalResponse.put("Code", "111");
        }
        return finalResponse;
    }

    public Map<String,Object> fetchAadhaarAndSaveAddress(String otp, String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("otp", otp);
        requestBody.put("requestId", requestId);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<HashMap> response = restTemplate.exchange(fetchOkycDataUrl, HttpMethod.POST, entity, HashMap.class);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        Map<String,Object> responsing = new HashMap<>();
        responsing.put("responseBody",responseBody);
        responsing.put("response", response);

        return responsing;
    }

}
