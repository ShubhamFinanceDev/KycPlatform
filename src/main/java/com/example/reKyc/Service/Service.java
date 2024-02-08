package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

public interface Service {

    HashMap<String, String> validateAndSendOtp(String loanNo);
    CustomerDetails getCustomerDetail(String mobileNo,String otpCode);
//    ResponseEntity<String> handleRequest(List<InputBase64> inputBase64);

    HashMap callFileExchangeServices(List<InputBase64.Base64Data> inputBase64,String documentType);
    boolean saveUpdatedDetails(UpdateAddress inputUpdateAddress);

    String enableProcessFlag(MultipartFile file);

    CustomerDetails checkLoanNo(String loanNo);

    CommonResponse updatCustomerKycFlag(String loanNo);
}
