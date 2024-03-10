package com.example.reKyc.ServiceImp;

import com.example.reKyc.Entity.Customer;
import com.example.reKyc.Entity.OtpDetails;
import com.example.reKyc.Entity.DdfsUpload;
import com.example.reKyc.Model.*;
import com.example.reKyc.Repository.CustomerRepository;
import com.example.reKyc.Repository.OtpDetailsRepository;
import com.example.reKyc.Repository.DdfsUploadRepository;
import com.example.reKyc.Service.LoanNoAuthentication;
import com.example.reKyc.Utill.*;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ServiceImp implements com.example.reKyc.Service.Service {

    @Autowired
    private LoanNoAuthentication loanNoAuthentication;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OtpDetailsRepository otpDetailsRepository;
    @Autowired
    private DdfsUploadRepository updatedDetailsRepository;
    @Autowired
    private OtpUtility otpUtility;
    @Autowired
    private DateTimeUtility dateTimeUtility;
    @Autowired
    private MaskDocumentAndFile maskDocumentAndFile;
    @Autowired
    private AadharAndPanUtility singzyServices;
    @Autowired
    AadharAndPanUtility externalApiServices;
    @Autowired
    private DdfsUtility ddfsUtility;
    @Value("${file_path}")
    String file_path;
    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    public HashMap validateAndSendOtp(String loanNo) {
        HashMap<String, String> otpResponse = new HashMap<>();
        CustomerDataResponse customerDetails = new CustomerDataResponse();
        try {

            Customer customer = customerRepository.getCustomer(loanNo);
            customerDetails = loanNoAuthentication.getCustomerData(customer.getLoanNumber());
            System.out.println(customerDetails.getMobileNumber());
            int otpCode = otpUtility.generateOtp(customerDetails);
            try {
                if (otpCode > 0) {
                    logger.info("otp generated successfully");
//                if (otpUtility.sendOtp(customerDetails.getMobileNumber(), otpCode)) {  //stopped sms services
                    logger.info("otp sent on mobile");
                    OtpDetails otpDetails = new OtpDetails();
                    otpDetails.setOtpCode(Long.valueOf(otpCode));
                    System.out.println(otpCode);
                    otpDetails.setMobileNo(customerDetails.getMobileNumber());

                    otpDetailsRepository.save(otpDetails);
                    Long otpId = otpDetails.getOtpId();
                    otpResponse.put("otpCode", String.valueOf(otpCode));
                    otpResponse.put("otpId", String.valueOf(otpId));
                    otpResponse.put("mobile", otpDetails.getMobileNo());
                    otpResponse.put("msg", "Otp send.");
                    otpResponse.put("code", "0000");

//                } else {
//                    otpResponse.put("msg", "Otp did not send, please try again");
//                    otpResponse.put("code", "1111");
//                }

                } else {
                    otpResponse.put("msg", "Otp did not generated");
                    otpResponse.put("code", "1111");
                }
            } catch (Exception e) {
                System.out.println("==exception while saving otp detail==");
                otpResponse.put("msg", "Technical issue");
                otpResponse.put("code", "1111");

            }

        } catch (Exception e) {
            System.out.println("==Loan not not found==");
            otpResponse.put("msg", "Loan no not found");
            otpResponse.put("code", "1111");

        }
        return otpResponse;
    }

    /**
     * @param mobileNo
     * @return
     */
    @Override
    public CustomerDataResponse otpValidation(String mobileNo, String otpCode, String loanNo) {

        CustomerDataResponse customerDetails = new CustomerDataResponse();
        try {
            OtpDetails otpDetails = otpDetailsRepository.IsotpExpired(mobileNo, otpCode);
            Duration duration = Duration.between(otpDetails.getOtpExprTime(), LocalDateTime.now());
//            customerDetails = (duration.toMinutes() > 50) ? customerDetails : customerDetailsRepository.findUserDetailByMobile(mobileNo, loanNo);
            customerDetails = (duration.toMinutes() > 50) ? customerDetails : loanNoAuthentication.getCustomerData(loanNo);

        } catch (Exception e) {
            System.out.println("===Otp invalid==");
        }
        return customerDetails;

    }

    /**
     * @param inputBase64
     * @return
     */


    @Override
    public HashMap callFileExchangeServices(List<InputBase64.Base64Data> inputBase64, String documentType) {

        HashMap<String, String> documentDetail = new HashMap<>();
        List<String> urls = new ArrayList<>();

        for (InputBase64.Base64Data base64 : inputBase64) {

            documentDetail = singzyServices.convertBase64ToUrl(base64.getFileType(), base64.getBase64String());
            if (documentDetail.containsKey("code")) {
                break;
            } else {
                urls.add(documentDetail.get("fileUrl"));
            }
        }
        if (!(urls.isEmpty())) {
            if (documentType.equals("aadhar")) {
                documentDetail = singzyServices.extractAadharDetails(urls);
            } else {
                documentDetail = singzyServices.extractPanDetails(urls);
            }
        }
        return documentDetail;
    }


//    public CustomerDetails checkExtractedDocumentId(String loanNo, String documentId, String documentType) {
//        CustomerDetails customerDetails = new CustomerDetails();
//        try {
//            if (documentType.equals("aadhar")) {
//
//                customerDetails = customerDetailsRepository.checkCustomerAadharNo(loanNo, documentId);
//
//            }
//            if (documentType.equals("pan")) {
//
//                customerDetails = customerDetailsRepository.checkCustomerPanNo(loanNo, documentId);
//            }
//
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        return customerDetails;
//
//    }



    /**
     * @param loanNo
     * @return
     */
    @Override
    public CommonResponse updateCustomerKycFlag(String loanNo) {

        CommonResponse commonResponse = new CommonResponse();
        try {
            Optional<Customer> customer = customerRepository.findById(loanNo);
            if (customer != null) {
                try {

                    customerRepository.updateKycFlag(loanNo);
                    commonResponse.setMsg("Successfully");
                    commonResponse.setCode("0000");
                } catch (Exception e) {
                    commonResponse.setMsg("Flag did not updated.");
                    commonResponse.setCode("1111");
                }
            }

        } catch (Exception e) {
            commonResponse.setMsg("Loan is not valid, try again");
            commonResponse.setCode("1111");
        }
        return commonResponse;
    }

    /**
     * @return
     */
    @Override
    public CommonResponse callDdfsService(UpdateAddress inputAddress, String applicationNO) {
        CommonResponse commonResponse = new CommonResponse();

        File folder = new File(file_path);
        File[] listOfFiles = folder.listFiles();
        String base64String = null;

        for (File file : listOfFiles) {
            if (file.getName().contains(inputAddress.getLoanNo())) {

                try {
                    byte[] fileBytes = Files.readAllBytes(Paths.get((file_path + file.getName())));
                    base64String = Base64.getEncoder().encodeToString(fileBytes);
//                System.out.println("Base64 representation of the file:\n" + base64String);
                    if (ddfsUtility.callDDFSApi(base64String, applicationNO)) {

                        if (saveUpdatedDetails(inputAddress, applicationNO)) {
                            System.out.println("=== data has been updated in db ===");
                        }
                    } else {
                        System.out.println("=== DDFS file upload exception ===");
                        commonResponse.setCode("1111");
                        commonResponse.setMsg("File upload error.");
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    commonResponse.setCode("1111");
                    commonResponse.setMsg("File upload error.");
                }
                System.out.println(file.getName());
            }

        }
        return commonResponse;
    }

    public boolean saveUpdatedDetails(UpdateAddress inputUpdateAddress, String fileName) {
        DdfsUpload updatedDetails = new DdfsUpload();
        updatedDetails.setMobileNo(inputUpdateAddress.getMobileNo());
        updatedDetails.setLoanNo(inputUpdateAddress.getLoanNo());
        updatedDetails.setFileName(fileName);
        updatedDetails.setDocumentType(inputUpdateAddress.getDocumentType());
        updatedDetails.setDdfsFlag("Y");

        try {
            updatedDetailsRepository.save(updatedDetails);
            return true;

        } catch (Exception e) {
            return false;

        }
    }

}
