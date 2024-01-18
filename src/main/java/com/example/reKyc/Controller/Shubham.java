package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.MaskDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public HashMap handleRequest(@RequestBody InputBase64 inputParam) {     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();
        HashMap<String, String> result = new HashMap<>();
        CustomerDetails customerDetails = new CustomerDetails();


        if (!(inputParam.getLoanNo().isBlank()) && !(inputParam.getDocumentId().isBlank()) && !(inputParam.getDocumentType().isBlank())) {

            for (InputBase64.Base64Data data : inputParam.getBase64Data()) {
                if (data.getFileType().isBlank() || data.getBase64String().isBlank()) {

                    extractDetail.put("code", "1111");
                    extractDetail.put("msg", "Required field is empty.");
                    break;
                }
            }

            if (!(extractDetail.containsKey("code"))) {
                customerDetails = service.checkExtractedDocumentId(inputParam.getLoanNo(), inputParam.getDocumentId(), inputParam.getDocumentType());

                if (customerDetails != null) {
                    extractDetail = service.callFileExchangeServices(inputParam.getBase64Data());
                    if (extractDetail.get("code").equals("0000")) {

                        boolean equality = maskDocument.compareAadharNoEquality(extractDetail.get("uid"), customerDetails.getAadhar());
//                        extractDetail.put("frontSide", inputParam.getBase64Data().get(0));
//                        extractDetail.put("backSide", inputParam.getBase64Data().get(1).toString());

                        if (!equality) {
                            extractDetail.clear();
                            extractDetail.put("msg", "aadhar no did not matched with document aadhar no.");
                            extractDetail.put("code", "1111");
                        }
                    }
                } else {
                    extractDetail.put("msg", "aadhar no and loan no is not matching.");
                    extractDetail.put("code", "1111");
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
        } else {
            response.put("msg", "Required field is empty");
            response.put("code", "1111");
        }
        return response;

    }


    @PostMapping("/verifyOtpForAadhar")
    public HashMap verifyOtp(@RequestBody AadharOtpVerifyInput inputParam) {
        HashMap<String, String> response = new HashMap<>();


        if (!(inputParam.getRequestID().isBlank()) && !(inputParam.getOtpCode().isBlank())) {
            response = service.verifyOtpAadhar(inputParam);
        } else {
            response.put("msg", "Required field is empty");
            response.put("code", "1111");
        }
        return response;
    }


    @PostMapping("/updateAddress")
    public ResponseEntity<CommonResponse> otpVerify(@RequestBody UpdateAddress inputUpdateAddress)
    {
        UpdateAddressResponse updateAddressResponse=new UpdateAddressResponse();
         CustomerDetails customerDetails=new CustomerDetails();

        if (inputUpdateAddress.getMobileNo().isBlank() || inputUpdateAddress.getOtpCode().isBlank()) {
            updateAddressResponse.setMsg("Required field is empty.");
            updateAddressResponse.setCode("1111");

        }
        else
        {
         customerDetails=  service.getCustomerDetail(inputUpdateAddress.getMobileNo(),inputUpdateAddress.getOtpCode());
         boolean saveStatus;
          if (customerDetails ==null)
          {
              updateAddressResponse.setMsg("Otp invalid or expire. please try again.");
              inputUpdateAddress.setOtpCode("1111");
          }
          else
          {
              saveStatus=service.saveUpdatedDetails(inputUpdateAddress);
          }
        }
        return new ResponseEntity<CommonResponse>(updateAddressResponse, HttpStatus.OK);
    }

}
