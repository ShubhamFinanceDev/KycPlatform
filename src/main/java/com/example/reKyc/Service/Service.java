package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Entity.UpdatedDetails;
import com.example.reKyc.Model.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Service {

    HashMap<String,String> validateAndSendOtp(String loanNo);
   CustomerDetails otpValidation(String mobileNo, String otpCode, String loanNo);
    HashMap<String,String> callFileExchangeServices(InputBase64 inputBase64, CustomerDetails customerDetails);
    CommonResponse updateCustomerKycFlag(String loanNo,String mobileNo);
    CommonResponse callDdfsService(UpdateAddress inputAddress, CustomerDetails applicationNo);
    KycCountUpload kycCount();

    CustomerDetails loanDetails(String loanNo);
    List<UpdatedDetails> getReportDataList();
    void generateExcel(HttpServletResponse response, List<UpdatedDetails> reportList);
    CommonResponse sendSmsOnMobile();

    Map<String, Object> getOkycOtp(String aadhaarNumber, String loanNumber);

    Map fetchOkycData(String otp, String requestId, String loanNumber);
}
