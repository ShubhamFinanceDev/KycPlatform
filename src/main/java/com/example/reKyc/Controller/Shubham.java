package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.AadharOtpInput;
import com.example.reKyc.Model.AadharOtpVerifyInput;
import com.example.reKyc.Model.InputBase64;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.MaskDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/shubham")
@CrossOrigin
public class Shubham {

    @Autowired
    private Service service;
    @Autowired
    private MaskDocument maskDocument;


    @PostMapping("/addressPreview")
    public HashMap handleRequest(@RequestBody InputBase64 inputParam){     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();
        HashMap<String,String> result=new HashMap<>();
        CustomerDetails customerDetails=new CustomerDetails();


        if(!(inputParam.getLoanNo().isBlank()) &&!(inputParam.getDocumentId().isBlank()) && !(inputParam.getDocumentType().isBlank())) {

            for (InputBase64.Base64Data data : inputParam.getBase64Data()) {
                if (data.getFileType().isBlank() || data.getBase64String().isBlank()) {

                    extractDetail.put("code", "1111");
                    extractDetail.put("msg", "Required field is empty.");
                    break;
                }
            }

            if(!(extractDetail.containsKey("code"))) {
                customerDetails = service.checkExtractedDocumentId(inputParam.getLoanNo(),inputParam.getDocumentId(),inputParam.getDocumentType());

                            if(customerDetails !=null) {
                                extractDetail = service.callFileExchangeServices(inputParam.getBase64Data());
                                if(extractDetail.get("code").equals("0000")) {
                                    boolean equality = maskDocument.compareAadharNoEquality(extractDetail.get("uid"), customerDetails.getAadhar());
                                    if (!equality) {
                                        extractDetail.clear();
                                        extractDetail.put("msg", "aadhar no did not matched with document aadhar no.");
                                        extractDetail.put("code", "1111");
                                    }
                                }
                            }
                            else
                            {
                                extractDetail.put("msg","aadhar and loan no is not matching.");
                                extractDetail.put("code","1111");
                            }
            }
        }


        return extractDetail;
    }



    @PostMapping("/sendOtpForAadhar")
    public HashMap invokeAddressPreviewService(@RequestBody AadharOtpInput inputParam) {
        HashMap<String, String> response = new HashMap<>();

        if (!(inputParam.getLoanNumber().isBlank()) && !(inputParam.getFileType().isBlank()) && !(inputParam.getAadharNo().isBlank())) {
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
