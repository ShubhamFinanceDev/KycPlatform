package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.AadharOtpInput;
import com.example.reKyc.Model.AadharOtpVerifyInput;
import com.example.reKyc.Model.InputBase64;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;

public interface Service {

    HashMap<String, String> validateSendOtp(String loanNo);
    CustomerDetails getCustomerDetail(String mobileNo);
    ResponseEntity<String> handleRequest(List<InputBase64> inputBase64);

    HashMap callFileExchangeServices(List<InputBase64> inputBase64);

    HashMap<String, String> getAddessByAadhar(AadharOtpInput inputParam);

    HashMap<String, String> verifyOtpAadhar(AadharOtpVerifyInput inputParam);

//    UserDetail getDataByUser(
}
