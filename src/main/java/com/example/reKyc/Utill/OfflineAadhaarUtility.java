package com.example.reKyc.Utill;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.ServiceImp.ServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class OfflineAadhaarUtility {

    @Value("${singzy.send.otp.aadhar}")
    private String getOkycOtpUrl;
    @Value("${singzy.verify.otp.aadhar}")
    private String fetchOkycDataUrl;
    @Value("${singzy.authorisation.key}")
    private String authorizationToken;
    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private ServiceImp serviceImp;

    public void processOkycResponse(Map<String, Object> result, String aadhaarNumber) {
        try {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("aadhaarNumber", aadhaarNumber), headers);
        ResponseEntity<Map> response = restTemplate.exchange(getOkycOtpUrl, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                result.put("msg", "successfully");
                result.put("code", "000");
                result.put("requestId", ((Map<?, ?>) responseBody.get("data")).get("requestId"));
            } else {
                result.put("msg", "Invalid response from OKYC service");
                result.put("code", "111");
            }
        } else {
            result.put("msg", "Technical issue, please try again");
            result.put("code", "1111");
        }

    } catch (Exception e){
            result.put("msg", "Exception found :"+e.getMessage());
            result.put("code", "111");
        }
    }

    public void processFetchOkycDataResponse(Map<String, Object> result, String otp, String requestId, CustomerDetails customerDetails) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("otp", otp, "requestId", requestId), headers);
            ResponseEntity<Map> response = restTemplate.exchange(fetchOkycDataUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    saveOkycData(responseBody, customerDetails);
                    if (responseBody.containsKey("statusCode") && responseBody.containsKey("message")) {
                        result.put("msg", "OKYC Data Fetched.");
                        result.put("code", "000");
                    } else {
                        result.put("msg", "OKYC Data NOT Fetched.");
                        result.put("code", "111");
                    }
                } else {
                    result.put("msg", "Invalid response from OKYC service");
                    result.put("code", "111");
                }
            } else {
                result.put("msg", "Technical issue, please try again");
                result.put("code", "111");
            }
    } catch (Exception e) {
        result.put("msg", "Exception found :"+e.getMessage());
        result.put("code", "111");
        }
    }

    private void saveOkycData(Map<String, Object> responseBody, CustomerDetails customerDetails) {
        Map<String, Object> data = (Map<String, Object>) responseBody.getOrDefault("data", Collections.emptyMap());
        Map<String, Object> addressData = (Map<String, Object>) data.getOrDefault("address", Collections.emptyMap());

        if (!addressData.isEmpty()) {
            List<String> addressComponents = new ArrayList<>();
            String[] componentOrder = {"vtc", "loc", "po", "dist", "state", "subdist", "street", "house", "landmark"};
            for (String component : componentOrder) {
                String value = (String) addressData.getOrDefault(component, "N/A");
                if (!value.equals("N/A") && !value.isEmpty()) {
                    addressComponents.add(value);
                }
            }
            String zip = (String) data.getOrDefault("zip", "N/A");
            if (!zip.equals("N/A")) {
                addressComponents.add(zip);
            }
            String concatenatedAddress = String.join(", ", addressComponents);
            System.out.println(concatenatedAddress);
            customerDetails.setAddressDetailsResidential(concatenatedAddress);
            serviceImp.updateCustomerDetails(Optional.of(customerDetails),"O");
        }
    }
}
