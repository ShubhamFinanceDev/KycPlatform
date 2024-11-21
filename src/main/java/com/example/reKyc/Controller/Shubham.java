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
import org.owasp.encoder.Encode;

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

    private String sanitizeInput(String input) {
        return Encode.forHtml(input);
    }

    @PostMapping("/upload-preview")
    public ResponseEntity<?> handleRequest(@RequestBody @Valid InputBase64 inputParam) {
        HashMap<String, String> extractDetail = new HashMap<>();
        try {
            // Validate and sanitize input fields
            String documentType = sanitizeInput(inputParam.getDocumentType());
            String loanNo = sanitizeInput(inputParam.getLoanNo());

            // Sanitize the documentId as well
            String documentId = sanitizeInput(inputParam.getDocumentId());

            CompletableFuture<CustomerDataResponse> customerDataResponse = fetchingDetails.getCustomerData(inputParam.getLoanNo());
            extractDetail = service.callFileExchangeServices(inputParam);
            CustomerDataResponse customerDataResponse1 = customerDataResponse.get();

            if (extractDetail.get("code").equals("0000")) {
                if (!loanNo.contains("_")) {
                    if (maskDocumentNo.compareDocumentNumber(customerDataResponse1, documentId, extractDetail.get("documentType"))) {
                        customerDataResponse1.setAddressDetailsResidential(extractDetail.get("address"));
                        service.updateCustomerDetails(customerDataResponse1, null, documentType);
                    } else {
                        extractDetail.clear();
                        extractDetail.put("msg", "The document id is invalid.");
                        extractDetail.put("code", "1111");
                    }
                } else {
                    customerDataResponse1.setAddressDetailsResidential(extractDetail.get("address"));
                    service.updateCustomerDetails(customerDataResponse1, null, documentType);
                }
            }
            return ResponseEntity.ok(extractDetail);

        } catch (Exception e) {
            extractDetail.clear();
            extractDetail.put("msg", "Technical issue, try again.");
            extractDetail.put("code", "1111");
            log.error("Error in handleRequest: " + e.getMessage());
            return new ResponseEntity<>(extractDetail, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload-kyc")
    public ResponseEntity<?> finalUpdate(@RequestBody @Valid UpdateAddress inputUpdateAddress) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            // Validate and sanitize input
            String mobileNo = sanitizeInput(inputUpdateAddress.getMobileNo());
            String loanNo = sanitizeInput(inputUpdateAddress.getLoanNo());
            String otpCode = sanitizeInput(inputUpdateAddress.getOtpCode());
            String applicationNo = sanitizeInput(inputUpdateAddress.getApplicationNo());

            if (service.otpValidation(mobileNo, otpCode, loanNo)) {
                commonResponse = service.callDdfsService(inputUpdateAddress, loanNo); // calls a service to update the address details
                if (commonResponse.getCode().equals("0000")) {
                    service.confirmationSmsAndUpdateKycStatus(loanNo, mobileNo, applicationNo);
                }
            } else {
                commonResponse.setMsg("Loan no or Otp is not valid.");
                commonResponse.setCode("1111");
            }
            return ResponseEntity.ok(commonResponse);
        } catch (Exception e) {
            commonResponse.setMsg("technical issue.");
            commonResponse.setCode("1111");
            log.error("Error in finalUpdate: " + e.getMessage());
            return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/disable-kyc-flag")
    public ResponseEntity<CommonResponse> disableKycFlag(@RequestBody @Valid Map<String, String> inputParam) {
        CommonResponse commonResponse = new CommonResponse();

        // Validate input parameters and sanitize
        String loanNo = sanitizeInput(inputParam.get("loanNo"));
        String mobileNo = sanitizeInput(inputParam.get("mobileNo"));

        if (loanNo == null || mobileNo == null) { // Validate required fields
            commonResponse.setMsg("One or more field is required");
            commonResponse.setCode("400");
            return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
        }

        commonResponse = service.updateCustomerKycFlag(loanNo, mobileNo); // Update the KYC flag for the customer
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }
}