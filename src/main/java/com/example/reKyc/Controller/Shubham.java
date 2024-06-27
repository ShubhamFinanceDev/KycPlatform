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

import java.util.HashMap;
import java.util.Map;


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


}
