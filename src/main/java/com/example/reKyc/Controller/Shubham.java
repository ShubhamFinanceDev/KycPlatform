package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Service.LoanNoAuthentication;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.OtpUtility;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@RestController
@RequestMapping("/shubham")
@CrossOrigin("*")
public class Shubham {

    @Autowired
    private Service service;
    @Autowired
    private LoanNoAuthentication loanNoAuthentication;
    @Autowired
    private OtpUtility otpUtility;

    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int randomStringLength = 5;
    private static final Random random = new SecureRandom();

    @PostMapping("/upload-preview")
    public HashMap<String,String> handleRequest(@RequestBody @Valid InputBase64 inputParam) {     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();
        try {
            CustomerDetails customerDetails = service.loanDetails(inputParam.getLoanNo()); //validate document type and ID
            String documentType = inputParam.getDocumentType();
            String documentId = inputParam.getDocumentId();
            if ((documentType.contains("pan") && customerDetails.getPan().equals(documentId)) || (documentType.contains("aadhar") && customerDetails.getAadhar().equals(documentId))) {
                extractDetail = service.callFileExchangeServices(inputParam,customerDetails);

            } else {
                extractDetail.put("msg", "The document ID number is incorrect");
                extractDetail.put("code", "1111");
            }

        } catch (Exception e) {
            extractDetail.put("msg", "Loan no is not valid.");
            extractDetail.put("code", "1111");
        }

        return extractDetail;
    }


    @PostMapping("/upload-kyc")
    public ResponseEntity<CommonResponse> finalUpdate(@RequestBody @Valid UpdateAddress inputUpdateAddress) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            CustomerDetails customerDetails = service.otpValidation(inputUpdateAddress.getMobileNo(), inputUpdateAddress.getOtpCode(), inputUpdateAddress.getLoanNo());   //Validate OTP and loan Number
            commonResponse = service.callDdfsService(inputUpdateAddress, customerDetails);   // calls a service to update the address details
            return ResponseEntity.ok(commonResponse);
        } catch (Exception e) {
            commonResponse.setMsg("Loan no or Otp is not valid.");
            commonResponse.setCode("1111");
            return ResponseEntity.ok(commonResponse);

        }
    }

    @PostMapping("/disable-kyc-flag")
    public ResponseEntity<CommonResponse> disableKycFlag(@RequestBody Map<String, String> inputParam) {
        CommonResponse commonResponse = new CommonResponse();

        if ((inputParam.get("loanNo") == null) || (inputParam.get("mobileNo") == null)) {    //validate input parameters
            commonResponse.setMsg("One or more field is required");
            commonResponse.setCode("400");
            return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);

        }
        commonResponse = service.updateCustomerKycFlag(inputParam.get("loanNo"), inputParam.get("mobileNo"));            //update the KYC flag for the customer

        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }


    @PostMapping("/get-Okyc-Otp")
    public ResponseEntity<Map<String, Object>> getOkycOtp(@RequestBody AadhaarRequest otpRequest) {
        Map<String, Object> response = service.getOkycOtp(otpRequest.getAadhaarNumber());

        // Extracting relevant data from the response
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        String requestId = (String) data.get("requestId");
        boolean otpSentStatus = (boolean) data.get("otpSentStatus");

        String modifiedRequestId = requestId + generateRandomString();

        //setting the message
        String statusMessage = otpSentStatus ? "SUCCESS" : "FAILED";

        //setting the msgCode
        String statusCode = otpSentStatus ? "1111" : "0000";

        // Creating the final response map
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("requestId", modifiedRequestId);
        finalResponse.put("Msg", statusMessage);
        finalResponse.put("Code", statusCode);
        finalResponse.put("originalResponse", response);

        return ResponseEntity.ok(finalResponse);
    }
    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(randomStringLength);
        for (int i = 0; i < randomStringLength; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }


    @PostMapping("/fetch-Okyc-Data")
    public ResponseEntity<Map<String, Object>> fetchOkycData(@RequestBody OkycDataRequest request){
        Map response = service.fetchOkycData(request.getOtp(), request.getRequestId());
        return ResponseEntity.ok(response);
    }


}
