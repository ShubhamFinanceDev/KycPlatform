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
    public HashMap<String, String> handleRequest(@RequestBody @Valid InputBase64 inputParam) {     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();

        CustomerDataResponse customerDetails = loanNoAuthentication.getCustomerData(inputParam.getLoanNo());
        if (customerDetails != null && ((inputParam.getDocumentType().contains("pan") && customerDetails.getPanNumber().equals(inputParam.getDocumentId())) || (inputParam.getDocumentType().contains("aadhar") && customerDetails.getAadharNumber().equals(inputParam.getDocumentId())))) {
            extractDetail = service.callFileExchangeServices(inputParam, inputParam.getDocumentType());      //convert file base 64 into url also extract details

        } else {
            extractDetail.put("msg", "The document ID number is incorrect");
            extractDetail.put("code", "1111");
        }

        return extractDetail;
    }


    @PostMapping("/updateAddress")
    public ResponseEntity<CommonResponse> finalUpdate(@RequestBody @Valid UpdateAddress inputUpdateAddress) {
        CommonResponse commonResponse = new CommonResponse();

        LoanDetails loanDetails = service.otpValidation(inputUpdateAddress.getMobileNo(), inputUpdateAddress.getOtpCode(), inputUpdateAddress.getLoanNo());

        if (loanDetails.getLoanNumber() == null) {
            commonResponse.setMsg("otp invalid or expire. please try again.");
            commonResponse.setCode("1111");
            return new ResponseEntity<>(commonResponse, HttpStatus.OK);
        } else {

            commonResponse = service.callDdfsService(inputUpdateAddress, loanDetails.getApplicationNumber(), loanDetails.getUserId());
        }

        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }

    @PostMapping("/disable-kyc-flag")
    public ResponseEntity<CommonResponse> disableKycFlag(@RequestBody Map<String, String> inputParam) {
        CommonResponse commonResponse = new CommonResponse();

        if ((!inputParam.containsKey("loanNo") && inputParam.get("loanNo") == null) && (!inputParam.containsKey("mobileNo") && inputParam.get("mobileNo") == null)) {
            commonResponse.setMsg("One or more field is required");
            commonResponse.setCode("400");
            return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);

        }
        commonResponse = service.updateCustomerKycFlag(inputParam.get("loanNo"));

        if (commonResponse.getCode().equals("0000"))
            otpUtility.sendTextMsg(inputParam.get("mobileNo"), SmsTemplate.existingKyc); //otp send

        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }


}
