package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Entity.UpdatedDetails;
import com.example.reKyc.Model.CommonResponse;
import com.example.reKyc.Model.GenerateReport;
import com.example.reKyc.Model.OtpRequest;
import com.example.reKyc.Model.JwtResponse;
import com.example.reKyc.Security.JwtHelper;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.MaskDocumentNo;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;
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

    @PostMapping("/send-otp")
    public ResponseEntity<HashMap> sendOtpOnRegisteredMobile(@RequestBody @Valid Map<String, String> inputParam) {
        logger.info("Received request to send OTP with input parameters : {}", inputParam);
        HashMap<String, String> otpResponse = new HashMap<>();
        String loanNo = inputParam.get("loanNo");
        if (loanNo.isEmpty() || loanNo == null) {
            logger.warn("Request to send OTP failed due to missing loanNo");
            otpResponse.put("msg", "One or more field is required");
            otpResponse.put("code", "400");
            return new ResponseEntity<>(otpResponse, HttpStatus.BAD_REQUEST);

        }
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
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLoanNo());   //load user details and( here userDetail interface is used)
            customerDetails = service.otpValidation(request.getMobileNo(), request.getOtpCode(), request.getLoanNo());
            if (customerDetails == null) {
                commonResponse.setMsg("Otp is expired or invalid.");
                commonResponse.setCode("1111");
                return ResponseEntity.ok(commonResponse);
            }
            token = this.jwtHelper.generateToken(userDetails);
            jwtResponse.setJwtToken(token);
            jwtResponse.setMobileNo(customerDetails.getMobileNumber());
            jwtResponse.setAddress(customerDetails.getAddressDetailsResidential());
            jwtResponse.setName(customerDetails.getCustomerName());
            jwtResponse.setPanNo(customerDetails.getPan() != null ? maskDocument.documentNoEncryption(customerDetails.getPan()) : "NA");
            jwtResponse.setAadharNo(customerDetails.getAadhar() != null ? maskDocument.documentNoEncryption(customerDetails.getAadhar()) : "NA");
            jwtResponse.setLoanNo(customerDetails.getLoanNumber());
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            commonResponse.setMsg("Loan no not found.");
            commonResponse.setCode("1111");
            return ResponseEntity.ok(commonResponse);
        }

    }

}


