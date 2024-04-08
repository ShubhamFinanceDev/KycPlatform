package com.example.reKyc.Service;

import com.example.reKyc.Entity.LoanDetails;
import com.example.reKyc.Model.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface Service {

    HashMap<String, String> validateAndSendOtp(String loanNo);
   Optional< LoanDetails> otpValidation(String mobileNo, String otpCode, String loanNo);
    HashMap<String,String> callFileExchangeServices(InputBase64 inputBase64,String documentType);
    CommonResponse updateCustomerKycFlag(String loanNo);
    CommonResponse callDdfsService(UpdateAddress inputAddress, String applicationNo,Long loanId);
    KycCountUpload kycCount();
}
