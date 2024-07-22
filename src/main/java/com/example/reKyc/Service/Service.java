package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Entity.UpdatedDetails;
import com.example.reKyc.Model.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface Service {

    HashMap<String,String> validateAndSendOtp(String loanNo);
    boolean otpValidation(String mobileNo, String otpCode, String loanNo);
    HashMap<String,String> callFileExchangeServices(InputBase64 inputBase64);
    CommonResponse updateCustomerKycFlag(String loanNo,String mobileNo);
    CommonResponse callDdfsService(UpdateAddress inputAddress, String loanNo);
    KycCountUpload kycCount();
     void updateCustomerDetails(Optional<CustomerDetails> loanDetails, String status);
    CustomerDetails loanDetails(String loanNo);
    List<UpdatedDetails> getReportDataList();
    void generateExcel(HttpServletResponse response, List<UpdatedDetails> reportList);
    CommonResponse sendSmsOnMobile();
}
