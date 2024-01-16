package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.CommonResponse;
import com.example.reKyc.Model.JwtRequest;
import com.example.reKyc.Model.JwtResponse;
import com.example.reKyc.Repository.OtpDetailsRepository;
import com.example.reKyc.Security.JwtHelper;
import com.example.reKyc.Service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/userKyc")

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
    private Logger logger = LoggerFactory.getLogger(User.class);
    @PostMapping("/sendOtp")
    public HashMap sendOtpOnRegisteredMobile(@RequestBody Map<String,String> inputParam)
    {
        String loanNo=  inputParam.get("loanNo");
        HashMap<String,String> otpResponse= new HashMap<>();

        if(loanNo.isEmpty())
        {
            otpResponse.put("msg","Loan number field is empty");
            otpResponse.put("code","1111");
        }
        else {
            otpResponse= service.validateSendOtp(loanNo);
        }

        return otpResponse;
    }



    @PostMapping("/otpVerification")
        public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        JwtResponse jwtResponse = new JwtResponse();
        if(request.getMobileNo().isBlank() || request.getOtpCode().isBlank())
        {
            jwtResponse.setMsg("Required filed is empty.");
            jwtResponse.setCode("1111");
            return new ResponseEntity<>(jwtResponse, HttpStatus.OK);

        }
        else {
            this.doAuthenticate(request.getMobileNo(), request.getOtpCode());

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getMobileNo());
            CustomerDetails customerDetails = service.getCustomerDetail(request.getMobileNo());


            String token = this.jwtHelper.generateToken(userDetails);


            jwtResponse.setJwtToken(token);
            jwtResponse.setMobileNo(userDetails.getUsername());
            jwtResponse.setAddress(customerDetails.getAddressDetailsResidential());
            jwtResponse.setName(customerDetails.getCustomerName());
            jwtResponse.setPanNo(customerDetails.getPAN());
            jwtResponse.setAadharNo(customerDetails.getAadhar());

            return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
        }
        }

        private void doAuthenticate(String email, String password) {

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
            try {
                authenticationManager.authenticate(authentication);

            } catch (BadCredentialsException e) {
                throw new BadCredentialsException(" Invalid Username or Password  !!");

            }

        }

        @ExceptionHandler(BadCredentialsException.class)
        public CommonResponse exceptionHandler() {
            CommonResponse commonResponse=new CommonResponse();
            commonResponse.setCode("1111");
            commonResponse.setMsg("invalid otp or expire.");
            return commonResponse;
        }
}


