package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.MaskDocumentAndFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;


@RestController
@RequestMapping("/shubham")
@CrossOrigin
public class Shubham {

    @Autowired
    private Service service;
    @Autowired
    private MaskDocumentAndFile maskDocument;


    @PostMapping("/addressPreview")
    public HashMap handleRequest(@RequestBody InputBase64 inputParam) {     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();
        CustomerDetails customerDetails = new CustomerDetails();

        try {
            if (!(inputParam.getLoanNo() == null || inputParam.getLoanNo().isBlank()) && !(inputParam.getDocumentId() == null || inputParam.getDocumentId().isBlank()) && !(inputParam.getDocumentType() == null || inputParam.getDocumentType().isBlank())) {

                for (InputBase64.Base64Data data : inputParam.getBase64Data()) {
                    if ((data.getFileType() == null || data.getFileType().isBlank()) || (data.getBase64String() == null || data.getBase64String().isBlank())) {

                        extractDetail.put("code", "1111");
                        extractDetail.put("msg", "required field is empty.");
                        return extractDetail;
                    }
                }

                customerDetails = service.checkExtractedDocumentId(inputParam.getLoanNo(), inputParam.getDocumentId(), inputParam.getDocumentType());
                if (customerDetails.getLoanNumber() != null) {
                    extractDetail = service.callFileExchangeServices(inputParam.getBase64Data());      //convert file base 64 into url also extract details

                    if (extractDetail.get("code").equals("0000")) {
                        boolean equality = maskDocument.compareAadharNoEquality(extractDetail.get("uid"), customerDetails.getAadhar()); //check extracted documented with registered documenteid
                        if (!equality) {
                            extractDetail.clear();
                            extractDetail.put("msg", "aadhar number did not matched with document aadhar no.");
                            extractDetail.put("code", "1111");
                        }
                        boolean fileStatus = maskDocument.generateFileLocally(inputParam);        //create a file  in local system
                        if (!fileStatus) {
                            extractDetail.clear();
                            extractDetail.put("msg", "something went wrong. please try again");
                            extractDetail.put("code", "1111");
                        }
                    }

                } else {
                    extractDetail.put("msg", "aadhar number is not matching with loan number.");
                    extractDetail.put("code", "1111");
                }


            } else {
                extractDetail.put("code", "1111");
                extractDetail.put("msg", "required field is empty.");
            }
            return extractDetail;

        } catch (Exception e) {
            extractDetail.put("code", "1111");
            extractDetail.put("msg", "Technical issue");
            return extractDetail;
        }
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
    public ResponseEntity<UpdateAddressResponse> finalUpdate(@RequestBody UpdateAddress inputUpdateAddress) {
        UpdateAddressResponse updateAddressResponse = new UpdateAddressResponse();
        CustomerDetails customerDetails = new CustomerDetails();

        if (((inputUpdateAddress.getMobileNo().isBlank() || inputUpdateAddress.getMobileNo() == null) || (inputUpdateAddress.getOtpCode().isBlank()
                || inputUpdateAddress.getUpdatedAddress() == null) || (inputUpdateAddress.getLoanNo().isBlank() || inputUpdateAddress.getLoanNo() == null) || (inputUpdateAddress.getDocumentType().isBlank() || inputUpdateAddress.getDocumentType() == null) || (inputUpdateAddress.getDocumentId().isBlank() || inputUpdateAddress.getDocumentId() == null))) {

            updateAddressResponse.setMsg("required field is empty.");
            updateAddressResponse.setCode("1111");
            return new ResponseEntity(updateAddressResponse,HttpStatus.OK);
        } else {
            customerDetails = service.getCustomerDetail(inputUpdateAddress.getMobileNo(), inputUpdateAddress.getOtpCode());
            boolean saveStatus;

            if (customerDetails.getLoanNumber() == null) {
                updateAddressResponse.setMsg("otp invalid or expire. please try again.");
                updateAddressResponse.setCode("1111");
                return new ResponseEntity(updateAddressResponse, HttpStatus.OK);

            } else {

                if (service.saveUpdatedDetails(inputUpdateAddress)) {

                    if (maskDocument.createFileInDffs(customerDetails.getLoanNumber())) {
                        updateAddressResponse.setMsg("E-KYC completed successfully.");
                        inputUpdateAddress.setOtpCode("0000.");
                    } else {
                        updateAddressResponse.setMsg("Something went wrong. please try again.");
                        updateAddressResponse.setCode("1111");
                    }
                } else {
                    updateAddressResponse.setMsg("Something went wrong. please try again.");
                    updateAddressResponse.setCode("1111");
                }
            }

        }
        return new ResponseEntity(updateAddressResponse, HttpStatus.OK);
    }

}
