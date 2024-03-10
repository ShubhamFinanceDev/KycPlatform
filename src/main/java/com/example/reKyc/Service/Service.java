package com.example.reKyc.Service;

import com.example.reKyc.Model.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

public interface Service {

    HashMap<String, String> validateAndSendOtp(String loanNo);
    CustomerDataResponse otpValidation(String mobileNo,String otpCode,String loanNo);
    HashMap callFileExchangeServices(List<InputBase64.Base64Data> inputBase64,String documentType);
    CommonResponse updateCustomerKycFlag(String loanNo);
    CommonResponse callDdfsService(UpdateAddress inputAddress, String applicationNo);
}
