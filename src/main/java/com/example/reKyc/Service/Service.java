package com.example.reKyc.Service;

import com.example.reKyc.Entity.LoanDetails;
import com.example.reKyc.Model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface Service {

    HashMap<String,String> validateAndSendOtp(String loanNo);
   LoanDetails otpValidation(String mobileNo, String otpCode, String loanNo);
    HashMap<String,String> callFileExchangeServices(InputBase64 inputBase64);
    CommonResponse updateCustomerKycFlag(String loanNo,String mobileNo);
    CommonResponse callDdfsService(UpdateAddress inputAddress, String applicationNo);
    KycCountUpload kycCount();

    LoanDetails loanDetails(String loanNo);
}
