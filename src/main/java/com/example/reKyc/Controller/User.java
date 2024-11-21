package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Security.JwtHelper;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.FetchingDetails;
import com.example.reKyc.Utill.MaskDocumentNo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.owasp.encoder.Encode;

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
    private FetchingDetails fetchingDetails;
    @Autowired
    private MaskDocumentNo maskDocument;

    private final Logger logger = LoggerFactory.getLogger(User.class);

    private String sanitizeInput(String input) {
        return Encode.forHtml(input); // Use OWASP Encoder to prevent XSS
    }

    @PostMapping("/send-otp")
    public ResponseEntity<HashMap> sendOtpOnRegisteredMobile(@RequestBody @Valid Map<String, String> inputParam) {
        logger.info("Received request to send OTP with input parameters : {}", inputParam);
        HashMap<String, String> otpResponse = new HashMap<>();
        String loanNo = inputParam.get("loanNo");

        if (loanNo == null || loanNo.trim().isEmpty()) {
            logger.warn("Request to send OTP failed due to missing or invalid loanNo");
            otpResponse.put("msg", "One or more fields are required");
            otpResponse.put("code", "400");
            return new ResponseEntity<>(otpResponse, HttpStatus.BAD_REQUEST);
        }

        // Sanitize input before passing to the service
        loanNo = sanitizeInput(loanNo);
        otpResponse = service.validateAndSendOtp(loanNo);
        return ResponseEntity.ok(otpResponse);
    }

    @PostMapping("/otp-verification")
    public ResponseEntity<?> login(@RequestBody @Valid OtpRequest request) {
        logger.info("Received request to login with input parameters : {}", request);
        CommonResponse commonResponse = new CommonResponse();
        JwtResponse jwtResponse = new JwtResponse();
        String token = null;
        CustomerDetails customerDetails;

        try {
            // Sanitize loanNo and mobileNo to prevent XSS or other malicious inputs
            String loanNo = sanitizeInput(request.getLoanNo());
            String mobileNo = sanitizeInput(request.getMobileNo());

            CompletableFuture<CustomerDataResponse> kycCustomerResponse = fetchingDetails.getCustomerData(loanNo);
            UserDetails userDetails = userDetailsService.loadUserByUsername(loanNo);   // Load user details
            boolean otp = service.otpValidation(mobileNo, request.getOtpCode(), loanNo);

            if (!otp) {
                commonResponse.setMsg("OTP is expired or invalid.");
                commonResponse.setCode("1111");
                return ResponseEntity.ok(commonResponse);
            }

            token = this.jwtHelper.generateToken(userDetails);
            CustomerDataResponse kycCustomer = kycCustomerResponse.get();

            // Set sanitized response data
            jwtResponse.setJwtToken(token);
            jwtResponse.setMobileNo(kycCustomer.getPhoneNumber());
            jwtResponse.setAddress(kycCustomer.getAddressDetailsResidential());
            jwtResponse.setName(kycCustomer.getCustomerName());
            jwtResponse.setPanNo(kycCustomer.getPanNumber());
            jwtResponse.setAadharNo(kycCustomer.getAadharNumber());
            jwtResponse.setVoterIdNo(kycCustomer.getVoterIdNumber());
            jwtResponse.setLoanNo(loanNo);

            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            commonResponse.setMsg("Loan number not found or invalid.");
            commonResponse.setCode("1111");
            return ResponseEntity.ok(commonResponse);
        }
    }
}