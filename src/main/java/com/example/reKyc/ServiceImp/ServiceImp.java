package com.example.reKyc.ServiceImp;

import com.example.reKyc.Entity.Customer;
import com.example.reKyc.Entity.LoanDetails;
import com.example.reKyc.Entity.OtpDetails;
import com.example.reKyc.Entity.DdfsUpload;
import com.example.reKyc.Model.*;
import com.example.reKyc.Repository.CustomerRepository;
import com.example.reKyc.Repository.LoanDetailsRepository;
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
    private LoanDetailsRepository loanDetailsRepository;
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

    public HashMap<String, String> validateAndSendOtp(String loanNo) {
        HashMap<String, String> otpResponse = new HashMap<>();
        CustomerDataResponse customerDetails;
        try {

            Customer customer = customerRepository.getCustomer(loanNo);
            customerDetails = loanNoAuthentication.getCustomerData(customer.getLoanNumber());
            int otpCode = otpUtility.generateOtp(customerDetails.getMobileNumber());

            if (otpCode > 0 && saveOtpDetail(otpCode, customerDetails.getMobileNumber()) && saveCustomerDetails(customerDetails)) {

//                if (otpUtility.sendOtp(customerDetails.getMobileNumber(), otpCode)) {  //stopped sms services
                logger.info("otp sent on mobile");
                otpResponse.put("otpCode", String.valueOf(otpCode));
                otpResponse.put("mobile", customerDetails.getMobileNumber());
                otpResponse.put("msg", "Otp send.");
                otpResponse.put("code", "0000");

//                } else {
//                    otpResponse.put("msg", "Otp did not send, please try again");
//                    otpResponse.put("code", "1111");
//                }

            } else {
                otpResponse.put("msg", "Loan no not found");
                otpResponse.put("code", "1111");

            }
        } catch (Exception e) {
            System.out.println("==Loan no not found==");
            otpResponse.put("msg", "Loan no not found");
            otpResponse.put("code", "1111");

        }
        return otpResponse;
    }

    private boolean saveOtpDetail(int otpCode, String mobileNo) {

        OtpDetails otpDetails = new OtpDetails();
        otpDetails.setOtpCode(Long.valueOf(otpCode));
        System.out.println(otpCode);
        otpDetails.setMobileNo(mobileNo);
        try {

            otpDetailsRepository.save(otpDetails);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private boolean saveCustomerDetails(CustomerDataResponse customerDetails) {
        LoanDetails loanDetails = new LoanDetails();

        loanDetails.setLoanNumber(customerDetails.getLoanNumber());
        loanDetails.setApplicationNumber(customerDetails.getApplicationNumber());
        loanDetails.setAadhar(customerDetails.getAadharNumber());
        loanDetails.setPan(customerDetails.getPanNumber());
        loanDetails.setCustomerName(customerDetails.getCustomerName());
        loanDetails.setAddressDetailsResidential(customerDetails.getAddressDetailsResidential());
        loanDetails.setMobileNumber(customerDetails.getMobileNumber());
        try {
            loanDetailsRepository.save(loanDetails);
            return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;

        }

    }


    @Override
    public LoanDetails otpValidation(String mobileNo, String otpCode, String loanNo) {

        LoanDetails loanDetails = new LoanDetails();
        try {
            OtpDetails otpDetails = otpDetailsRepository.IsotpExpired(mobileNo, otpCode);
            Duration duration = Duration.between(otpDetails.getOtpExprTime(), LocalDateTime.now());
            loanDetails = (duration.toMinutes() > 50) ? loanDetails : loanDetailsRepository.getLoanDetail(loanNo);

        } catch (Exception e) {
            System.out.println("===Otp invalid==");
        }
        return loanDetails;

    }

    /**
     *
     */


    @Override
    public HashMap<String, String> callFileExchangeServices(InputBase64 inputBase64, String documentType) {

        HashMap<String, String> documentDetail = new HashMap<>();
        List<String> urls = new ArrayList<>();
        try {


            for (InputBase64.Base64Data base64 : inputBase64.getBase64Data()) {

                documentDetail = singzyServices.convertBase64ToUrl(base64.getFileType(), base64.getBase64String());
                if (documentDetail.containsKey("code")) {
                    break;
                } else {
                    urls.add(documentDetail.get("fileUrl"));
                }
            }
            if (!(urls.isEmpty())) {
                if (documentType.equals("aadhar")) {
                    documentDetail = singzyServices.extractAadharDetails(urls, inputBase64.getDocumentId());
                } else {
                    documentDetail = singzyServices.extractPanDetails(urls, inputBase64.getDocumentId());
                }
            }
        } catch (Exception e) {

            documentDetail.put("code", "1111");
            documentDetail.put("msg", "Technical issue");
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
            if (customer.isPresent()) {
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

                        otpUtility.sendTextMsg(inputAddress.getMobileNo(), SmsTemplate.updationKyc); //otp send

                    } else {
                        System.out.println("=== DDFS file upload exception ===");
                        commonResponse.setCode("1111");
                        commonResponse.setMsg("File upload error.");
                        break;
                    }

                } catch (Exception e) {
                    System.out.println(e);
                    commonResponse.setCode("1111");
                    commonResponse.setMsg("File upload error.");
                }
                System.out.println(file.getName());
            }

        }
        return commonResponse;
    }

    @Override
    public HashMap<String, String> validateAndConfirmSendOtp(String loanNo) {
        HashMap<String, String> otpResponse = new HashMap<>();
        try {

            LoanDetails loanDetails = loanDetailsRepository.getLoanDetail(loanNo);
            int otpCode = otpUtility.generateOtp(loanDetails.getLoanNumber());

            if (otpCode > 0 && saveOtpDetail(otpCode, loanDetails.getMobileNumber())) {

//                if (otpUtility.sendOtp(loanDetails.getMobileNumber(), otpCode,loanNo)) {  //stopped sms services
                logger.info("otp sent on mobile");
                otpResponse.put("otpCode", String.valueOf(otpCode));
                otpResponse.put("mobile", loanDetails.getMobileNumber());
                otpResponse.put("msg", "Otp send.");
                otpResponse.put("code", "0000");

//                } else {
//                    otpResponse.put("msg", "Otp did not send, please try again");
//                    otpResponse.put("code", "1111");
//                }

            } else {
                otpResponse.put("msg", "Loan no not found");
                otpResponse.put("code", "1111");
            }
        } catch (Exception e) {
            System.out.println("==Loan no not found==");
            otpResponse.put("msg", "Loan no not found");
            otpResponse.put("code", "1111");
        }

        return otpResponse;
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
