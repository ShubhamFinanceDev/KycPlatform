package com.example.reKyc.Controller;

import com.example.reKyc.Model.AadharOtpInput;
import com.example.reKyc.Model.AadharOtpVerifyInput;
import com.example.reKyc.Model.InputBase64;
import com.example.reKyc.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/shubham")
public class Shubham {

    @Autowired
    private Service service;
    @PostMapping("/addressPreview")
    public HashMap handleRequest(@RequestBody List<InputBase64> inputBase64, @RequestHeader("Authorization") String authorization){     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();


        Boolean inputStatus = false;
        for (InputBase64 inputParam : inputBase64) {
            if (!(inputParam.getDocumentType().isBlank()) && !(inputParam.getBase64String().isBlank())) {
                inputStatus = true;
            } else {
                inputStatus = false;
                extractDetail.put("code", "1111");
                extractDetail.put("msg", "Required field is empty.");
                break;
            }
        }
        if (inputStatus) {
            extractDetail = service.callFileExchangeServices(inputBase64);
        }


        return extractDetail;
    }



    @PostMapping("/sendOtpForAadhar")
    public HashMap invokeAddressPreviewService(@RequestBody AadharOtpInput inputParam) {
        HashMap<String, String> response = new HashMap<>();

        if (!(inputParam.getLoanNumber().isBlank()) && !(inputParam.getDocumentType().isBlank())) {
            response = service.getAddessByAadhar(inputParam);
        }
        else
        {
            response.put("msg","Required field is empty");
            response.put("code","1111");
        }
        return response;

    }


    @PostMapping("/verifyOtpForAadhar")
    public HashMap verifyOtp(@RequestBody AadharOtpVerifyInput inputParam)
    {
        HashMap<String, String> response = new HashMap<>();


        if (!(inputParam.getRequestID().isBlank()) && !(inputParam.getOtpCode().isBlank())) {
            response = service.verifyOtpAadhar(inputParam);
        }
        else
        {
            response.put("msg","Required field is empty");
            response.put("code","1111");
        }
        return response;
    }

}
