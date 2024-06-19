package com.example.reKyc.Utill;

import com.example.reKyc.Model.PanCardResponse;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRotY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Component
public class DdfsUtility {
    @Value("${ddfs.key}")
    private String passcode;
    @Value("${ddfs.url}")
    private String ddfsUrl;
    @Value("${neo.ip}")
    private String neo_ip;
    @Value("${ddfs.path}")
    private String path;


    RestTemplate restTemplate = new RestTemplate();

    private final Logger logger = LoggerFactory.getLogger(DdfsUtility.class);


    public String generateDDFSKey() throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 5);
        calendar.add(Calendar.MINUTE, 30);
        String formattedDate = dateFormat.format(calendar.getTime());

        String plainText = formattedDate + "@" + neo_ip;   // "localhost";
        String encryptedText = encrypt(plainText, passcode);
        System.out.println("Encrypted Text: " +
                encryptedText);
        String decryptedText = decrypt(encryptedText, passcode);
        System.out.println("Decrypted Text: " +
                decryptedText);
        return encryptedText;
    }

    public static String encrypt(String plainText, String secretKey) throws Exception {
        byte[] keyBytes = secretKey.getBytes("UTF-8");
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        String encryptedText = new String(
                org.apache.commons.codec.binary.Base64.encodeBase64(encryptedByte));
        return encryptedText;
    }

    public static String decrypt(String encryptedText, String secretKey) throws Exception {
        byte[] keyBytes = secretKey.getBytes("UTF-8");
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] encryptedTextByte =
                org.apache.commons.codec.binary.Base64.decodeBase64(encryptedText.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        String decryptedText = new String(decryptedByte);
        return decryptedText;
    }


    public Boolean callDDFSApi(String base64String, String applicationNo) {
        boolean status = false;
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        try {

            formData.add("token", generateDDFSKey());
            formData.add("clientId", "SHUBHAM/REKYC");
            formData.add("file", applicationNo);
            formData.add("subPath", "2024/APR");
            formData.add("docCategory", "Rekyc Document");
            formData.add("clientUserId", "06799");
            formData.add("remarks", "");
            formData.add("maker", "06799");
            formData.add("path", path);
            formData.add("document", base64String);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
            ResponseEntity<HashMap> responseBody = restTemplate.postForEntity(ddfsUrl, requestEntity, HashMap.class);

            if (responseBody.getStatusCode().toString().contains("200") && responseBody.getBody().get("status").toString().contains("SUCCESS")) {
//                System.out.println("Response from the DDFS API: " + responseBody.getBody().get("status"));
                logger.info("DDFS API Call Success");
                status = true;

            }
            System.out.println("ddfs response " + responseBody.getBody());
        } catch (Exception e) {
            System.out.println("==Error in DDFS api call");
            logger.error("DDFS API Call Error{}", e.getMessage());
        }
        return status;
    }

}

