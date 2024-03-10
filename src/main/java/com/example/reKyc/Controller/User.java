package com.example.reKyc.Controller;

import com.example.reKyc.Model.CommonResponse;
import com.example.reKyc.Model.CustomerDataResponse;
import com.example.reKyc.Model.OtpRequest;
import com.example.reKyc.Model.JwtResponse;
import com.example.reKyc.Repository.OtpDetailsRepository;
import com.example.reKyc.Security.JwtHelper;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.MaskDocumentAndFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/userKyc")
@CrossOrigin

public class User {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private OtpDetailsRepository otpDetailsRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Service service;

    @Autowired
    private MaskDocumentAndFile maskDocument;
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    private Logger logger = LoggerFactory.getLogger(User.class);

    @PostMapping("/sendOtp")
    public HashMap sendOtpOnRegisteredMobile(@RequestBody Map<String, String> inputParam) {
        String loanNo = inputParam.get("loanNo");
        HashMap<String, String> otpResponse = new HashMap<>();

        if (loanNo.isEmpty()) {
            otpResponse.put("msg", "Loan number field is empty");
            otpResponse.put("code", "1111");

        } else {
            otpResponse = service.validateAndSendOtp(loanNo);

        }
        return otpResponse;

    }


    @PostMapping("/otpVerification")
    public ResponseEntity<JwtResponse> login(@RequestBody OtpRequest request) {

        CommonResponse commonResponse = new CommonResponse();
        JwtResponse jwtResponse = new JwtResponse();
        if (request.getMobileNo().isBlank() || request.getOtpCode().isBlank()) {
            commonResponse.setMsg("Required field is empty.");
            commonResponse.setCode("1111");
            return new ResponseEntity(commonResponse, HttpStatus.OK);

        } else {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLoanNo());
                CustomerDataResponse customerDetails = service.otpValidation(request.getMobileNo(), request.getOtpCode(), request.getLoanNo());
                if (customerDetails.getLoanNumber() != null) {

                    String token = this.jwtHelper.generateToken(userDetails);

                    jwtResponse.setJwtToken(token);
                    jwtResponse.setMobileNo(customerDetails.getMobileNumber());
                    jwtResponse.setAddress(customerDetails.getAddressDetailsResidential());
                    jwtResponse.setName(customerDetails.getCustomerName());
                    jwtResponse.setPanNo(maskDocument.documentNoEncryption(customerDetails.getPanNumber()));
                    jwtResponse.setAadharNo(maskDocument.documentNoEncryption(customerDetails.getAadharNumber()));

                    jwtResponse.setLoanNo(customerDetails.getLoanNumber());

                } else {
                    commonResponse.setMsg("Otp is invalid or expired, please try again.");
                    commonResponse.setCode("1111");
                    return new ResponseEntity(commonResponse, HttpStatus.OK);

                }
                return new ResponseEntity(jwtResponse, HttpStatus.OK);

            } catch (Exception e) {
                commonResponse.setMsg("Mobile Number is not valid.");
                commonResponse.setCode("1111");
                return new ResponseEntity(commonResponse, HttpStatus.OK);
            }
        }
    }


}

