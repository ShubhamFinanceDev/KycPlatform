package com.example.reKyc.Controller;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Service.LoanNoAuthentication;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.FetchingDetails;
import com.example.reKyc.Utill.OtpUtility;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/shubham")
@CrossOrigin
public class Shubham {

    @Autowired
    private Service service;
    @Autowired
    private LoanNoAuthentication loanNoAuthentication;
    @Autowired
    private FetchingDetails fetchingDetails;

    @PostMapping("/upload-preview")
    public ResponseEntity<?> handleRequest(@RequestBody @Valid InputBase64 inputParam) {     //convert base64 into url
        HashMap<String, String> extractDetail = new HashMap<>();
        try {

            String documentType = inputParam.getDocumentType();
            String documentId = inputParam.getDocumentId();
            List<CustomerDetails> customerDetails = fetchingDetails.getCustomerIdentification(inputParam.getLoanNo()).get();
            System.out.println("call");
            extractDetail = service.callFileExchangeServices(inputParam);

            CustomerDetails customerDetailsResponseData = new CustomerDetails();
            for (CustomerDetails customerDetailsResponse : customerDetails) {
                if (customerDetailsResponse.getIdentificationType().contains(documentType)) {
                    if (!documentId.equals(documentId)) {
                        extractDetail.clear();
                        extractDetail.put("msg", "The document ID number is incorrect");
                        extractDetail.put("code", "1111");
                        break;
                    }
                    customerDetailsResponseData = customerDetailsResponse;
                }
            }
            customerDetailsResponseData.setResidentialAddress(extractDetail.get("address"));
            service.updateCustomerDetails(Optional.of(customerDetailsResponseData), null);
            return ResponseEntity.ok(extractDetail);

        } catch (Exception e) {
            extractDetail.put("msg", "Loan no is not valid.");
            extractDetail.put("code", "1111");
            return new ResponseEntity<>(extractDetail, HttpStatus.BAD_REQUEST);
        }

    }


    @PostMapping("/upload-kyc")
    public ResponseEntity<?> finalUpdate(@RequestBody @Valid UpdateAddress inputUpdateAddress) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            if(service.otpValidation(inputUpdateAddress.getMobileNo(), inputUpdateAddress.getOtpCode(), inputUpdateAddress.getLoanNo()))
            {
                commonResponse = service.callDdfsService(inputUpdateAddress,inputUpdateAddress.getLoanNo());   // calls a service to update the address details
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
