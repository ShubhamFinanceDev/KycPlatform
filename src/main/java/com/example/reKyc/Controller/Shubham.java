package com.example.reKyc.Controller;

import com.example.reKyc.Model.*;
import com.example.reKyc.Service.LoanNoAuthentication;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.MaskDocumentAndFile;
import com.example.reKyc.Utill.OtpUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/shubham")
@CrossOrigin
public class Shubham {

    @Autowired
    private Service service;
    @Autowired
    private MaskDocumentAndFile maskDocument;
    @Autowired
    private LoanNoAuthentication loanNoAuthentication;
    @Autowired
    private OtpUtility otpUtility;

    @PostMapping("/addressPreview")
    public HashMap handleRequest(@RequestBody InputBase64 inputParam) {     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();
        CustomerDataResponse customerDetails = new CustomerDataResponse();

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
                customerDetails = loanNoAuthentication.getCustomerData(inputParam.getLoanNo());

                if (customerDetails != null && ((inputParam.getDocumentType().contains("pan") && customerDetails.getPanNumber().equals(inputParam.getDocumentId())) || (inputParam.getDocumentType().contains("aadhar") && customerDetails.getAadharNumber().equals(inputParam.getDocumentId())))) {
                    extractDetail = service.callFileExchangeServices(inputParam.getBase64Data(), inputParam.getDocumentType());      //convert file base 64 into url also extract details

                    if (extractDetail.get("code").equals("0000")) {
                        boolean equality = maskDocument.compareDocumentNumber(extractDetail.get("uid"), inputParam.getDocumentId(), inputParam.getDocumentType()); //check extracted documented with registered documenteid
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
                    extractDetail.put("msg", "Entered mention id not correct");
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


    @PostMapping("/updateAddress")
    public ResponseEntity<CommonResponse> finalUpdate(@RequestBody UpdateAddress inputUpdateAddress) {
        CustomerDataResponse customerDetails = new CustomerDataResponse();
        CommonResponse commonResponse = new CommonResponse();

        if ((inputUpdateAddress.getMobileNo().isBlank() || inputUpdateAddress.getMobileNo() == null) || (inputUpdateAddress.getOtpCode().isBlank() || inputUpdateAddress.getOtpCode() == null) || (inputUpdateAddress.getLoanNo().isBlank() || inputUpdateAddress.getLoanNo() == null) || (inputUpdateAddress.getDocumentType().isBlank() || inputUpdateAddress.getDocumentType() == null) || (inputUpdateAddress.getDocumentId().isBlank() || inputUpdateAddress.getDocumentId() == null)) {

            commonResponse.setMsg("required field is empty.");
            commonResponse.setCode("1111");
        } else {
            customerDetails = service.otpValidation(inputUpdateAddress.getMobileNo(), inputUpdateAddress.getOtpCode(), inputUpdateAddress.getLoanNo());

            if (customerDetails.getLoanNumber() == null) {
                commonResponse.setMsg("otp invalid or expire. please try again.");
                commonResponse.setCode("1111");
                return new ResponseEntity(commonResponse, HttpStatus.OK);

            } else {

                commonResponse = service.callDdfsService(inputUpdateAddress, customerDetails.getApplicationNumber());

            }
        }
        return new ResponseEntity(commonResponse, HttpStatus.OK);
    }

    @PostMapping("/disable-kyc-flag")
    public ResponseEntity<CommonResponse> disableKycFlag(@RequestBody Map<String, String> inputParam) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            if (inputParam.containsKey("loanNo") && inputParam.containsKey("mobileNo")) {
                commonResponse = service.updateCustomerKycFlag(inputParam.get("loanNo"));
                otpUtility.sendOtp(inputParam.get("mobileNo"),"upToDate"); //otp send

            } else {
                commonResponse.setCode("1111");
                commonResponse.setMsg("Required fields are empty");
            }
            return new ResponseEntity<>(commonResponse, HttpStatus.OK);
        } catch (Exception e) {
            commonResponse.setCode("1111");
            commonResponse.setMsg("Something went wrong. please try again");
        }
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }


}
