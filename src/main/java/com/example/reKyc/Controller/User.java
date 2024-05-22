package com.example.reKyc.Controller;

import com.example.reKyc.Entity.LoanDetails;
import com.example.reKyc.Model.CommonResponse;
import com.example.reKyc.Model.OtpRequest;
import com.example.reKyc.Model.JwtResponse;
import com.example.reKyc.Security.JwtHelper;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.MaskDocumentNo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/userKyc")
@CrossOrigin
@Validated
public class User {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private Service service;

    @Autowired
    private MaskDocumentNo maskDocument;
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(User.class);

    @PostMapping("/sendOtp")
    public ResponseEntity<HashMap<String, String>> sendOtpOnRegisteredMobile(@RequestBody @Valid Map<String, String> inputParam) {
        logger.info("Received request to send OTP with input parameters : {}", inputParam);
        HashMap<String, String> otpResponse = new HashMap<>();

        if (!inputParam.containsKey("loanNo") && inputParam.get("loanNo") == null) {
            logger.warn("Request to send OTP failed due to mussing loanNo");
            otpResponse.put("msg", "One or more field is required");
            otpResponse.put("code", "400");
            return new ResponseEntity<>(otpResponse, HttpStatus.BAD_REQUEST);

        }
        otpResponse = service.validateAndSendOtp(inputParam.get("loanNo"));
        return new ResponseEntity<>(otpResponse, HttpStatus.OK);

    }


    @PostMapping("/otpVerification")
    public ResponseEntity<?> login(@RequestBody @Valid OtpRequest request) {

        CommonResponse commonResponse = new CommonResponse();
        JwtResponse jwtResponse = new JwtResponse();
        String token = null;
        LoanDetails loanDetails;

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLoanNo());
            loanDetails = service.otpValidation(request.getMobileNo(), request.getOtpCode(), request.getLoanNo());
            token = this.jwtHelper.generateToken(userDetails);

        } catch (Exception e) {
            commonResponse.setMsg("Mobile Number Or Otp is not valid.");
            commonResponse.setCode("1111");
            return ResponseEntity.ok(commonResponse);
        }
        jwtResponse.setJwtToken(token);
        jwtResponse.setMobileNo(loanDetails.getMobileNumber());
        jwtResponse.setAddress(loanDetails.getAddressDetailsResidential());
        jwtResponse.setName(loanDetails.getCustomerName());
        jwtResponse.setPanNo(loanDetails.getPan() != null ? maskDocument.documentNoEncryption(loanDetails.getPan()) : "NA");
        jwtResponse.setAadharNo(loanDetails.getAadhar() != null ? maskDocument.documentNoEncryption(loanDetails.getAadhar()) : "NA");
        jwtResponse.setLoanNo(loanDetails.getLoanNumber());
        return  ResponseEntity.ok(jwtResponse);
    }


}


