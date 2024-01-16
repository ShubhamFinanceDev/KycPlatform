package com.example.reKyc.ServiceImp;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Entity.OtpDetails;
import com.example.reKyc.Model.AadharOtpInput;
import com.example.reKyc.Model.AadharOtpVerifyInput;
import com.example.reKyc.Model.InputBase64;
import com.example.reKyc.Repository.CustomerDetailsRepository;
import com.example.reKyc.Repository.OtpDetailsRepository;
import com.example.reKyc.Utill.AuthToken;
import com.example.reKyc.Utill.DateTimeUtility;
import com.example.reKyc.Utill.ExternalApiServices;
import com.example.reKyc.Utill.OtpUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ServiceImp implements com.example.reKyc.Service.Service {

    @Autowired
    private CustomerDetailsRepository customerDetailsRepository;
    @Autowired
    private OtpDetailsRepository otpDetailsRepository;
    @Autowired
    private OtpUtility otpUtility;
    @Autowired
    private DateTimeUtility dateTimeUtility;
    @Autowired
    private AuthToken authToken;
    @Autowired
    private ExternalApiServices singzyServices;
    @Autowired
    ExternalApiServices externalApiServices;

    BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
    public HashMap validateSendOtp(String loanNo) {
        HashMap<String, String> otpResponse = new HashMap<>();
        CustomerDetails customerDetails = new CustomerDetails();
        try {


            customerDetails = customerDetailsRepository.findByLoanNumber(loanNo);
            if (!(customerDetails == null)) {
                int count = otpDetailsRepository.countByMobile(customerDetails.getMobileNumber());

                if (count > 0) {

                    otpDetailsRepository.deletePreviousOtp(customerDetails.getMobileNumber());
                }
                int otpCode = otpUtility.generateOtp();

//                if (otpUtility.sendOtp(customerDetails.getMobileNumber(), otpCode)) {  //stopped sms services
                OtpDetails otpDetails = new OtpDetails();
                otpDetails.setOtpCode(Long.valueOf(otpCode));

                System.out.println(otpCode);
                otpDetails.setOtpPassword(bCryptPasswordEncoder.encode(String.valueOf(otpCode)));
                otpDetails.setMobileNo(customerDetails.getMobileNumber());


                String timeStamp = dateTimeUtility.otpExpiryTime();
                otpDetails.setOtpExprTime(timeStamp);
                otpDetailsRepository.save(otpDetails);
                Long otpId = otpDetails.getOtpId();

                otpResponse.put("otpCode", String.valueOf(otpCode));
                otpResponse.put("otpId", String.valueOf(otpId));
                otpResponse.put("mobile", otpDetails.getMobileNo());
                otpResponse.put("msg", "Otp send.");
                otpResponse.put("code", "0000");
            } else {
                otpResponse.put("msg", "Otp did not send.");
                otpResponse.put("code", "1111");
            }

//            } else
//                {
//                    otpResponse.put("msg", "Loan no not found");
//                    otpResponse.put("code", "1111");
//
//                }

        } catch (Exception e) {
            System.out.println(e);
        }
        return otpResponse;
    }

    /**
     * @param mobileNo
     * @return
     */
    @Override
    public CustomerDetails getCustomerDetail(String mobileNo) {
        CustomerDetails customerDetails=customerDetailsRepository.findUserDetailByMobile(mobileNo);
        return customerDetails;
    }

    @Override
    public ResponseEntity<String> handleRequest(List<InputBase64> inputBase64) {
        return null;
    }


    private CustomerDetails findUserDetail(String mobileNo) {
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails = customerDetailsRepository.findUserDetailByMobile(mobileNo);
        if (customerDetails.getPAN() != null) {
            customerDetails.setPAN(authToken.documentNoEncryption(customerDetails.getPAN()));
        }

        if (customerDetails.getAadhar() != null) {
            customerDetails.setAadhar(authToken.documentNoEncryption(customerDetails.getAadhar()));
        }

        return customerDetails;
    }


    @Override
    public HashMap callFileExchangeServices(List<InputBase64> inputBase64) {

        HashMap<String, String> documentDetail = new HashMap<>();
        List<String> urls = new ArrayList<>();

        for (InputBase64 base64 : inputBase64) {

            documentDetail = singzyServices.convertBase64ToUrl(base64.getDocumentType(), base64.getBase64String());
            if (documentDetail.containsKey("code")) {
                break;
            } else {
                urls.add(documentDetail.get("fileUrl"));
            }
        }
        if (!(urls.isEmpty())) {
            documentDetail = singzyServices.extractAadharDetails(urls);
        }
        return documentDetail;
    }


    public HashMap getAddessByAadhar(AadharOtpInput inputParam) {

        HashMap<String, String> aadhaNumberDetails = new HashMap<>();
        int status= customerDetailsRepository.checkAadharNo(inputParam.getLoanNumber(),inputParam.getAadharNo());
        if(status>0)
        {
//            aadhaNumberDetails.put("aadhar no", inputParam.getAadharNo());
            aadhaNumberDetails=singzyServices.sendOtpOnLinkMobileNO(inputParam.getAadharNo());

        }
        else
        {
            aadhaNumberDetails.put("code","1111");
            aadhaNumberDetails.put("msg","Aadhar number is not valid.");
        }
        return aadhaNumberDetails;
    }

    /**
     * @param inputParam
     * @return
     */
    @Override
    public HashMap<String, String> verifyOtpAadhar(AadharOtpVerifyInput inputParam) {
        return singzyServices.validateOtp(inputParam.getRequestID(),inputParam.getOtpCode());
    }


    /**
     * @return
     */

}
