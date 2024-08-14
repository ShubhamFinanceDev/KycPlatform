package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Entity.UpdatedDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Security.JwtHelper;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.FetchingDetails;
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
import java.util.concurrent.CompletableFuture;

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
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(User.class);

    @PostMapping("/send-otp")
    public ResponseEntity<HashMap<String, String>> sendOtpOnRegisteredMobile(@RequestBody @Valid Map<String, String> inputParam) {
        logger.info("Received request to send OTP with input parameters : {}", inputParam);
        HashMap<String, String> otpResponse = new HashMap<>();
        String loanNo = inputParam.get("loanNo");

        // Validate the loan number format (e.g., should not be empty and should follow a specific pattern)
        if (loanNo == null || loanNo.isEmpty() || !loanNo.matches("^[a-zA-Z0-9_]+$")) {
            logger.warn("Request to send OTP failed due to invalid loanNo: {}", loanNo);
            otpResponse.put("msg", "Invalid loan number format");
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
            CompletableFuture<CustomerDataResponse> kycCustomerResponse = fetchingDetails.getCustomerData(request.getLoanNo());
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLoanNo());   //load user details and( here userDetail interface is used)
            boolean otp = service.otpValidation(request.getMobileNo(), request.getOtpCode(), request.getLoanNo());
            if (!otp) {
                commonResponse.setMsg("Otp is expired or invalid.");
                commonResponse.setCode("1111");
                return ResponseEntity.ok(commonResponse);
            }
            token = this.jwtHelper.generateToken(userDetails);
            CustomerDataResponse kycCustomer=kycCustomerResponse.get();
            jwtResponse.setJwtToken(token);
            jwtResponse.setMobileNo(kycCustomer.getPhoneNumber());
            jwtResponse.setAddress(kycCustomer.getAddressDetailsResidential());
            jwtResponse.setName(kycCustomer.getCustomerName());
            jwtResponse.setPanNo(kycCustomer.getPanNumber());
            jwtResponse.setAadharNo(kycCustomer.getAadharNumber());
            jwtResponse.setLoanNo(request.getLoanNo());
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            commonResponse.setMsg("Loan no not found.");
            commonResponse.setCode("1111");
            return ResponseEntity.ok(commonResponse);
        }

    }

}


