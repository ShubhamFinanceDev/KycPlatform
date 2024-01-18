package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.AadharOtpInput;
import com.example.reKyc.Model.AadharOtpVerifyInput;
import com.example.reKyc.Model.InputBase64;
import com.example.reKyc.Model.UpdateAddress;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;

public interface Service {

    HashMap<String, String> validateAndSendOtp(String loanNo);
    CustomerDetails getCustomerDetail(String mobileNo,String otpCode);
//    ResponseEntity<String> handleRequest(List<InputBase64> inputBase64);

    HashMap callFileExchangeServices(List<InputBase64.Base64Data> inputBase64);

    HashMap<String, String> getAddressByAadhar(AadharOtpInput inputParam);

    HashMap<String, String> verifyOtpAadhar(AadharOtpVerifyInput inputParam);

    CustomerDetails checkExtractedDocumentId(String loanNo, String documentId,String documentType );

    boolean saveUpdatedDetails(UpdateAddress inputUpdateAddress);

//    UserDetail getDataByUser(
}
