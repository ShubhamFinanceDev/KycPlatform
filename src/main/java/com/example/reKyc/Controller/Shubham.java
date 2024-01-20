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
        CustomerDetails customerDetails = new CustomerDetails();


        if (!( inputParam.getLoanNo()==null ||  inputParam.getLoanNo().isBlank()) && !(inputParam.getDocumentId()==null || inputParam.getDocumentId().isBlank()) && !(inputParam.getDocumentType()==null || inputParam.getDocumentType().isBlank())) {

            for (InputBase64.Base64Data data : inputParam.getBase64Data()) {
                if (data.getFileType().isBlank() || data.getBase64String().isBlank()) {

                    extractDetail.put("code", "1111");
                    extractDetail.put("msg", "required field is empty.");
                    break;
                }
            }

            if (!(extractDetail.containsKey("code"))) {
                customerDetails = service.checkExtractedDocumentId(inputParam.getLoanNo(), inputParam.getDocumentId(), inputParam.getDocumentType());

                if (customerDetails != null) {
                    extractDetail = service.callFileExchangeServices(inputParam.getBase64Data());
                    if (extractDetail.get("code").equals("0000")) {

                        boolean equality = maskDocument.compareAadharNoEquality(extractDetail.get("uid"), customerDetails.getAadhar());

                        if (!equality) {
                            extractDetail.clear();
                            extractDetail.put("msg", "aadhar no did not matched with document aadhar no.");
                            extractDetail.put("code", "1111");
                        }

                    }
                } else {
                    extractDetail.put("msg", "aadhar number is not matching with loan number.");
                    extractDetail.put("code", "1111");
                }
            }
        }
        else
        {
            extractDetail.put("code", "1111");
            extractDetail.put("msg", "required field is empty.");
        }


        return extractDetail;
    }


    @PostMapping("/sendOtpForAadhar")
    public HashMap invokeAddressPreviewService(@RequestBody AadharOtpInput inputParam) {
        HashMap<String, String> response = new HashMap<>();

        if (!(inputParam.getLoanNumber().isBlank()) && !(inputParam.getFileType().isBlank()) && !(inputParam.getAadharNo().isBlank())) {
            response = service.getAddressByAadhar(inputParam);
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
    public ResponseEntity<UpdateAddressResponse> finalUpdate(@RequestBody UpdateAddress inputUpdateAddress)
    {
        UpdateAddressResponse updateAddressResponse=new UpdateAddressResponse();
         CustomerDetails customerDetails=new CustomerDetails();

        if (((inputUpdateAddress.getMobileNo().isBlank() ||inputUpdateAddress.getMobileNo()==null ) || (inputUpdateAddress.getOtpCode().isBlank()
                || inputUpdateAddress.getUpdatedAddress()== null ) || (inputUpdateAddress.getLoanNo().isBlank() || inputUpdateAddress.getLoanNo()==null )|| (inputUpdateAddress.getDocumentType().isBlank() || inputUpdateAddress.getDocumentType()==null ) || (inputUpdateAddress.getDocumentId().isBlank() || inputUpdateAddress.getDocumentId()==null))) {

            updateAddressResponse.setMsg("required field is empty.");
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

                if(service.saveUpdatedDetails(inputUpdateAddress))
                {
                    updateAddressResponse.setMsg("E-KYC completed successfully.");
                    inputUpdateAddress.setOtpCode("0000.");
                }
              else {
                    updateAddressResponse.setMsg("Something went wrong. please try again.");
                    updateAddressResponse.setCode("1111");
              }
          }

        }
        return new ResponseEntity<UpdateAddressResponse>(updateAddressResponse, HttpStatus.OK);
    }

}
