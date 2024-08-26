package com.example.reKyc.Utill;


import com.example.reKyc.Model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AadharAndPanUtility {

    @Autowired
    private MaskDocumentNo maskDocumentAndFile;
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
    @Value("${signzy.aadhar.maskingUrl}")
    private String maskingUrl;
    @Value("${signzy.extraction.voterId}")
    private String voterIdUrl;

    private final Logger logger = LoggerFactory.getLogger(DdfsUtility.class);


    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();


    public HashMap<String, String> convertBase64ToUrl(String documentType, String base64String) {


        HashMap<String, String> inputBody = new HashMap<>();
        inputBody.put("base64String", base64String);
        inputBody.put("mimetype", documentType);
        inputBody.put("ttl", fileExchangeBase64Ttl);
        inputBody.put("sizw", "10MB");
        HashMap<String, String> urlResponse = new HashMap<>();


        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", singzyAuthKey);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(inputBody, headers);
//        System.out.println("request" + inputBody);

        try {
            ResponseEntity<ResponseOfBase64> responseOfBase64 = restTemplate.postForEntity(fileExchangeBase64Url, requestEntity, ResponseOfBase64.class);

            if (responseOfBase64.getStatusCode() == HttpStatus.OK) {
//                System.out.println(responseOfBase64.getBody().getFile().directURL);
                String url = responseOfBase64.getBody().getFile().directURL;
                urlResponse.put("fileUrl", url);
                logger.info("Url converted to base64: ");

            } else {
                urlResponse.put("code", "1111");
                urlResponse.put("msg", "Technical issue, please try again");

            }
        } catch (Exception e) {
            urlResponse.put("code", "1111");
            urlResponse.put("msg", "Technical issue, please try again");
            logger.error("Error while converting Base64 to url: {}", e.getMessage());
        }

        return urlResponse;
    }

    public HashMap<String, String> extractAadharDetails(List<String> urls, String documentId) {

        HashMap<String, List> inputBody = new HashMap<>();
        inputBody.put("files", urls);
        HashMap<String, String> addressPreview = new HashMap<>();
        try {

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", singzyAuthKey);
            HttpEntity<Map<String, List>> requestEntity = new HttpEntity<>(inputBody, headers);

            ResponseEntity<AadharResponse> aadharResponseBody = restTemplate.postForEntity(extractAadharUrl, requestEntity, AadharResponse.class);

            if (aadharResponseBody.getStatusCode() == HttpStatus.OK) {
                AadharResponse aadharResponse = aadharResponseBody.getBody();
                if (aadharResponse != null && !(aadharResponse.getResult().getUid().isBlank()) && !(aadharResponse.getResult().getName().isBlank()) && !(aadharResponse.getResult().getAddress().isBlank()) && aadharResponse.getResult().isValidBackAndFront()) {
                    if (maskDocumentAndFile.compareDocumentNumber(aadharResponse.getResult().getUid(), documentId, "aadhar")) {
//                    System.out.println(aadharResponse);
                        addressPreview.put("code", "0000");
                        addressPreview.put("msg", "File extracted successfully");
                        addressPreview.put("name", aadharResponse.getResult().getName());
                        addressPreview.put("address", aadharResponse.getResult().getAddress());
                        addressPreview.put("dateOfBirth", aadharResponse.getResult().getDateOfBirth());
                        addressPreview.put("uid", aadharResponse.getResult().getUid());
                        logger.info("Extracted Aadhar Details: ");
                    } else {
                        addressPreview.put("msg", "The document ID number is incorrect.");
                        addressPreview.put("code", "1111");
                    }

                } else {
                    addressPreview.put("code", "1111");
                    addressPreview.put("msg", "Uploaded file is not valid, please try again");
                }
            } else {
                addressPreview.put("code", "1111");
                addressPreview.put("msg", "Technical issue, please try again");
            }

        } catch (Exception e) {
            addressPreview.put("code", "1111");
            addressPreview.put("msg", "Technical issue, please try again");
            logger.error("Error extracting Aadhar Details: {}", e.getMessage());
        }
        return addressPreview;
    }

    public HashMap<String, String> extractPanDetails(List<String> urls, String documentId) {

        HashMap<String, Object> inputBody = new HashMap<>();
        inputBody.put("files", urls);
        inputBody.put("type", "individualPan");
        inputBody.put("getRelativeData", true);
        HashMap<String, String> panResponse = new HashMap<>();

        try {

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", singzyAuthKey);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(inputBody, headers);
//            System.out.println("request" + inputBody);
            ResponseEntity<PanCardResponse> extractPanResponse = restTemplate.postForEntity(extractPanUrl, requestEntity, PanCardResponse.class);

            if (extractPanResponse.getStatusCode() == HttpStatus.OK) {
                PanCardResponse panCardResponse = extractPanResponse.getBody();
                if (maskDocumentAndFile.compareDocumentNumber(panCardResponse.getResult().getNumber(), documentId, "pan")) {

                    System.out.println("Response-" + extractPanResponse.getBody().getResult());
                    System.out.println("staus" + extractPanResponse.getStatusCode());
                    panResponse.put("code", "0000");
                    panResponse.put("msg", "File extracted successfully");
                    panResponse.put("name", extractPanResponse.getBody().getResult().getName());
                    panResponse.put("dateOfBirth", extractPanResponse.getBody().getResult().getDob());
                    panResponse.put("uid", extractPanResponse.getBody().getResult().getNumber());
                    logger.info("Extract pan details :");
                } else {
                    panResponse.put("code", "1111");
                    panResponse.put("msg", "File did not extracted, please try again");
                }
            } else {
                panResponse.put("code", "1111");
                panResponse.put("msg", "Technical issue, please try again");
            }

        } catch (Exception e) {
            panResponse.put("code", "1111");
            panResponse.put("msg", "Technical issue, please try again");
            logger.error("Error extracting pan details :{}", e.getMessage());
        }
        return panResponse;
    }
    @Async
    public CompletableFuture<List<String>> maskAadhar(List<String> unmaskedUrl) throws Exception {
        List<String> maskedUrls = new ArrayList<>();
        HashMap<String, Object> urlRequest = new HashMap<>();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", singzyAuthKey);

        for (String url : unmaskedUrl) {
            List<String> unmaskedUrls1= new ArrayList<>();
            unmaskedUrls1.add(url);
            urlRequest.put("requestType", true);
            urlRequest.put("urls", unmaskedUrls1);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(urlRequest, headers);
            ResponseEntity<HashMap> maskingResponse = restTemplate.postForEntity(maskingUrl, requestEntity, HashMap.class);
            if (maskingResponse.getStatusCode() == HttpStatus.OK) {
                Map<?, ?> maskedResponse = (Map<?, ?>) Objects.requireNonNull(maskingResponse.getBody()).get("result");

                if (maskedResponse.get("isMasked").equals("yes")) {
                    logger.info("Masking process completed");
                    List<?> urls1 = (List<?>) maskedResponse.get("maskedImages");
                    maskedUrls.add((String) urls1.get(0));
                } else {
                    logger.info("Masking process failed");
                }

        }
        }
        return CompletableFuture.completedFuture(maskedUrls);
    }


    public HashMap<String, String> extractVoterIdDetails(List<String> urls, String documentId) {

        HashMap<String, Object> inputBody = new HashMap<>();
        inputBody.put("urls", urls);
        HashMap<String,String> voterIdResponse=new HashMap<>();
        try {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", singzyAuthKey);
            HttpEntity<HashMap<String,Object>> requestEntity = new HttpEntity<>(inputBody, headers);

            ResponseEntity<HashMap> voterIdExtractionResponse = restTemplate.postForEntity(voterIdUrl, requestEntity, HashMap.class);

            if (voterIdExtractionResponse.getStatusCode() == HttpStatus.OK) {
                System.out.println("Response-" + voterIdExtractionResponse.getBody());
                Map<?, ?> voterIdDetails = (Map<?, ?>) Objects.requireNonNull(voterIdExtractionResponse.getBody()).get("result");

                if (maskDocumentAndFile.compareDocumentNumber((String) voterIdDetails.get("epicNumber"), documentId, "voterId")) {
                    voterIdResponse.put("code", "0000");
                    voterIdResponse.put("msg", "File extracted successfully");
                    voterIdResponse.put("name", (String) voterIdDetails.get("name"));
                    voterIdResponse.put("dateOfBirth", (String) voterIdDetails.get("dob"));
                    voterIdResponse.put("uid", (String) voterIdDetails.get("epicNumber"));
//                String address = (String) voterIdDetails.get("address");
                    voterIdResponse.put("address", (String) voterIdDetails.get("address"));

                    logger.info("Extract voterId details {}", voterIdResponse);
                }else {
                    voterIdResponse.put("msg", "The document ID number is incorrect.");
                    voterIdResponse.put("code", "1111");
                }
            }
            else
            {
                voterIdResponse.put("code", "1111");
                voterIdResponse.put("msg", "Technical issue, please try again");
            }

        } catch (Exception e) {
            voterIdResponse.put("code", "1111");
            voterIdResponse.put("msg", "Technical issue, please try again");
            logger.error("Error extracting voterId details :{}", e.getMessage());
        }
        return voterIdResponse;
    }


}
