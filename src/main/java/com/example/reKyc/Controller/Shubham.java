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

//                customerDetails = service.checkExtractedDocumentId(inputParam.getLoanNo(), inputParam.getDocumentId(), inputParam.getDocumentType());
                customerDetails = service.checkLoanNo(inputParam.getLoanNo());

                if (customerDetails != null && (customerDetails.getPan().equals(inputParam.getDocumentId()) || customerDetails.getAadhar().equals(inputParam.getDocumentId()))) {
                    extractDetail = service.callFileExchangeServices(inputParam.getBase64Data(),inputParam.getDocumentType());      //convert file base 64 into url also extract details

                    if (extractDetail.get("code").equals("0000")) {
                        boolean equality = maskDocument.compareDocumentNumber(extractDetail.get("uid"), inputParam.getDocumentId(),inputParam.getDocumentType()); //check extracted documented with registered documenteid
                        if (!equality) {
                            extractDetail.clear();
                            extractDetail.put("msg", "uploaded document is not matching with loan number.");
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
                    extractDetail.put("msg", "Loan no not found or document id did not");
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


//    @PostMapping("/sendOtpForAadhar")
//    public HashMap invokeAddressPreviewService(@RequestBody AadharOtpInput inputParam) {
//        HashMap<String, String> response = new HashMap<>();
//
//        if (!(inputParam.getLoanNumber().isBlank()) && !(inputParam.getFileType().isBlank()) && !(inputParam.getAadharNo().isBlank())) {
//            response = service.getAddressByAadhar(inputParam);
//        } else {
//            response.put("msg", "Required field is empty");
//            response.put("code", "1111");
//        }
//        return response;
//
//    }
//
//
//    @PostMapping("/verifyOtpForAadhar")
//    public HashMap verifyOtp(@RequestBody AadharOtpVerifyInput inputParam) {
//        HashMap<String, String> response = new HashMap<>();
//
//
//        if (!(inputParam.getRequestID().isBlank()) && !(inputParam.getOtpCode().isBlank())) {
//            response = service.verifyOtpAadhar(inputParam);
//        } else {
//            response.put("msg", "Required field is empty");
//            response.put("code", "1111");
//        }
//        return response;
//    }


    @PostMapping("/updateAddress")
    public ResponseEntity<CommonResponse> finalUpdate(@RequestBody UpdateAddress inputUpdateAddress) {
        CustomerDetails customerDetails = new CustomerDetails();
        CommonResponse commonResponse=new CommonResponse();

        if (((inputUpdateAddress.getMobileNo().isBlank() || inputUpdateAddress.getMobileNo() == null) || (inputUpdateAddress.getOtpCode().isBlank()
                || inputUpdateAddress.getUpdatedAddress() == null) || (inputUpdateAddress.getLoanNo().isBlank() || inputUpdateAddress.getLoanNo() == null) || (inputUpdateAddress.getDocumentType().isBlank() || inputUpdateAddress.getDocumentType() == null) || (inputUpdateAddress.getDocumentId().isBlank() || inputUpdateAddress.getDocumentId() == null))) {

            commonResponse.setMsg("required field is empty.");
            commonResponse.setCode("1111");
            return new ResponseEntity(commonResponse,HttpStatus.OK);
        } else {
            customerDetails = service.getCustomerDetail(inputUpdateAddress.getMobileNo(), inputUpdateAddress.getOtpCode());

            if (customerDetails.getLoanNumber() == null) {
                commonResponse.setMsg("otp invalid or expire. please try again.");
                commonResponse.setCode("1111");
                return new ResponseEntity(commonResponse, HttpStatus.OK);

            } else {

                if (service.saveUpdatedDetails(inputUpdateAddress)) {

                    if (maskDocument.createFileInDffs(customerDetails.getLoanNumber())) {
                        commonResponse.setMsg("E-KYC completed successfully.");
                        commonResponse.setCode("0000.");
                    } else {
                        commonResponse.setMsg("Something went wrong. please try again.");
                        commonResponse.setCode("1111");
                    }
                } else {
                    commonResponse.setMsg("Something went wrong. please try again.");
                    commonResponse.setCode("1111");
                }
            }

        }
        return new ResponseEntity(commonResponse, HttpStatus.OK);
    }

}
