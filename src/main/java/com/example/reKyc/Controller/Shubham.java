package com.example.reKyc.Controller;

import com.example.reKyc.Model.*;
import com.example.reKyc.Service.LoanNoAuthentication;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.FetchingDetails;
import com.example.reKyc.Utill.MaskDocumentNo;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/shubham")
@CrossOrigin
public class Shubham {

    private static final Logger log = LoggerFactory.getLogger(Shubham.class);
    @Autowired
    private Service service;
    @Autowired
    private LoanNoAuthentication loanNoAuthentication;
    @Autowired
    private FetchingDetails fetchingDetails;
    @Autowired
    private MaskDocumentNo maskDocumentNo;

    @PostMapping("/upload-preview")
    public ResponseEntity<?> handleRequest(@RequestBody @Valid InputBase64 inputParam) {     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();
        try {

            String documentType = inputParam.getDocumentType();
            CompletableFuture<CustomerDataResponse> customerDataResponse = fetchingDetails.getCustomerData(inputParam.getLoanNo());
            extractDetail = service.callFileExchangeServices(inputParam);
            CustomerDataResponse customerDataResponse1=customerDataResponse.get();

            if(extractDetail.get("code").equals("0000")) {
                if(!inputParam.getLoanNo().contains("_")) {
                    if (maskDocumentNo.compareDocumentNumber(customerDataResponse1, inputParam.getDocumentId(), extractDetail.get("documentType"))) {
                        customerDataResponse1.setAddressDetailsResidential(extractDetail.get("address"));
                        service.updateCustomerDetails(customerDataResponse1, null, documentType);
                    } else {
                        extractDetail.clear();
                        extractDetail.put("msg", "The document id is invalid.");
                        extractDetail.put("code", "1111");

                    }
                }
                else
                {
                    customerDataResponse1.setAddressDetailsResidential(extractDetail.get("address"));
                    service.updateCustomerDetails(customerDataResponse1, null, documentType);
                }
            }
            return ResponseEntity.ok(extractDetail);

        } catch (Exception e) {
            extractDetail.clear();
            extractDetail.put("msg", "Technical issue, try again.");
            extractDetail.put("code", "1111");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(extractDetail, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @PostMapping("/upload-kyc")
    public ResponseEntity<?> finalUpdate(@RequestBody @Valid UpdateAddress inputUpdateAddress) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            if(service.otpValidation(inputUpdateAddress.getMobileNo(), inputUpdateAddress.getOtpCode(), inputUpdateAddress.getLoanNo()))
            {
                commonResponse = service.callDdfsService(inputUpdateAddress,inputUpdateAddress.getLoanNo());   // calls a service to update the address details
                if(commonResponse.getCode().equals("0000")) {
                    service.confirmationSmsAndUpdateKycStatus(inputUpdateAddress.getLoanNo(), inputUpdateAddress.getMobileNo());
                }
            }
            else{
                commonResponse.setMsg("Loan no or Otp is not valid.");
                commonResponse.setCode("1111");
            }
            return ResponseEntity.ok(commonResponse);
        } catch (Exception e) {
            commonResponse.setMsg("technical issue.");
            commonResponse.setCode("1111");
            return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    @PostMapping("/disable-kyc-flag")
    public ResponseEntity<CommonResponse> disableKycFlag(@RequestBody Map<String, String> inputParam) {
        CommonResponse commonResponse = new CommonResponse();

        if ((inputParam.get("loanNo") == null) || (inputParam.get("mobileNo") == null)) {    //validate input parameters
            commonResponse.setMsg("One or more field is required");
            commonResponse.setCode("400");
            return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);

        }
        commonResponse = service.updateCustomerKycFlag(inputParam.get("loanNo"), inputParam.get("mobileNo"));            //update the KYC flag for the customer
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }


}
