package com.example.reKyc.Controller;

import com.example.reKyc.Entity.LoanDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Service.LoanNoAuthentication;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.OtpUtility;
import com.example.reKyc.Utill.SmsTemplate;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/shubham")
@CrossOrigin
public class Shubham {

    @Autowired
    private Service service;
    @Autowired
    private LoanNoAuthentication loanNoAuthentication;
    @Autowired
    private OtpUtility otpUtility;

    @PostMapping("/addressPreview")
    public ResponseEntity<HashMap<String, String>> handleRequest(@RequestBody @Valid InputBase64 inputParam) {     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();
        LoanDetails loanDetails;
        try {
            loanDetails = service.loanDetails(inputParam.getLoanNo());
        } catch (Exception e) {
            extractDetail.put("msg", "Loan no is not valid.");
            extractDetail.put("code", "1111");
            return new ResponseEntity<>(extractDetail, HttpStatus.OK);
        }

        if ((inputParam.getDocumentType().contains("pan") && loanDetails.getPan().equals(inputParam.getDocumentId())) || (inputParam.getDocumentType().contains("aadhar") && loanDetails.getAadhar().equals(inputParam.getDocumentId()))) {
            extractDetail = service.callFileExchangeServices(inputParam, inputParam.getDocumentType());      //convert file base 64 into url also extract details
        } else {
            extractDetail.put("msg", "The document ID number is incorrect");
            extractDetail.put("code", "1111");
        }
        return new ResponseEntity<>(extractDetail, HttpStatus.OK);

    }


    @PostMapping("/updateAddress")
    public ResponseEntity<CommonResponse> finalUpdate(@RequestBody @Valid UpdateAddress inputUpdateAddress) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            LoanDetails loanDetails = service.otpValidation(inputUpdateAddress.getMobileNo(), inputUpdateAddress.getOtpCode(), inputUpdateAddress.getLoanNo());
            commonResponse = service.callDdfsService(inputUpdateAddress, loanDetails.getApplicationNumber(), loanDetails.getUserId());
            return ResponseEntity.ok(commonResponse);
        } catch (Exception e) {
            commonResponse.setMsg("Loan no Or Otp is not valid.");
            commonResponse.setCode("1111");
            return ResponseEntity.ok(commonResponse);

        }
    }

    @PostMapping("/disable-kyc-flag")
    public ResponseEntity<CommonResponse> disableKycFlag(@RequestBody Map<String, String> inputParam) {
        CommonResponse commonResponse = new CommonResponse();

        if ((!inputParam.containsKey("loanNo") && inputParam.get("loanNo") == null) && (!inputParam.containsKey("mobileNo") && inputParam.get("mobileNo") == null)) {
            commonResponse.setMsg("One or more field is required");
            commonResponse.setCode("400");
            return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);

        }
        commonResponse = service.updateCustomerKycFlag(inputParam.get("loanNo"),inputParam.get("mobileNo"));

        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }


}
