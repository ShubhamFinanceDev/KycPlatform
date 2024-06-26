package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Entity.UpdatedDetails;
import com.example.reKyc.Model.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;

public interface Service {

    HashMap<String,String> validateAndSendOtp(String loanNo);
   CustomerDetails otpValidation(String mobileNo, String otpCode, String loanNo);
    HashMap<String,String> callFileExchangeServices(InputBase64 inputBase64);
    CommonResponse updateCustomerKycFlag(String loanNo,String mobileNo);
    CommonResponse callDdfsService(UpdateAddress inputAddress, CustomerDetails applicationNo);
    KycCountUpload kycCount();

    CustomerDetails loanDetails(String loanNo);
    List<UpdatedDetails> getReportDataList();
    void generateExcel(HttpServletResponse response, List<UpdatedDetails> reportList);

    void sendOtpOnContactLists(List<String> contactList);
}
