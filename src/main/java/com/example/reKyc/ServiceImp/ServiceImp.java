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
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.codec.binary.Base64;

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
    private DdfsUploadRepository ddfsUploadRepository;
    @Autowired
    private OtpUtility otpUtility;
    @Autowired
    private MaskDocumentNo maskDocumentAndFile;
    @Autowired
    private AadharAndPanUtility singzyServices;
    @Autowired
    AadharAndPanUtility externalApiServices;
    @Autowired
    private DdfsUtility ddfsUtility;
    @Value("${file_path}")
    String file_path;
    Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    public HashMap<String, String> validateAndSendOtp(String loanNo) {
        HashMap<String, String> otpResponse = new HashMap<>();

        try {

            Customer customer = customerRepository.getCustomer(loanNo);
            if (customer != null) {
                String phoneNo = saveCustomerDetails(customer.getLoanNumber());
                int otpCode = otpUtility.generateOtp(phoneNo);
                if ((otpCode > 0 && phoneNo != null) && saveOtpDetail(otpCode, phoneNo)) {
//                    if (otpUtility.sendOtp(phoneNo, otpCode, loanNo)) {  //stopped sms services
                    logger.info("otp sent on mobile");
                    otpResponse.put("otpCode", String.valueOf(otpCode));
                    otpResponse.put("mobile", phoneNo);
                    otpResponse.put("msg", "Otp send.");
                    otpResponse.put("code", "0000");

//                    } else {
//                        otpResponse.put("msg", "Please try again");
//                        otpResponse.put("code", "1111");
//                    }

                } else {
                    otpResponse.put("msg", "Please try again");
                    otpResponse.put("code", "1111");
                }
            } else {
                System.out.println("==Loan no not found==");
                otpResponse.put("msg", "Loan no not found");
                otpResponse.put("code", "1111");
            }

        } catch (Exception e) {
            System.out.println(e);
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

    private String saveCustomerDetails(String loanNo) {
        String phoneNo = null;

        try {
            Optional<LoanDetails> loanDetails = loanDetailsRepository.getLoanDetail(loanNo);
            if (loanDetails.isEmpty()) {
                LoanDetails loanDetails1 = new LoanDetails();
                CustomerDataResponse customerDetails = loanNoAuthentication.getCustomerData(loanNo);
                phoneNo = customerDetails.getMobileNumber();
                loanDetails1.setLoanNumber(customerDetails.getLoanNumber());
                loanDetails1.setApplicationNumber(customerDetails.getApplicationNumber());
                loanDetails1.setAadhar(customerDetails.getAadharNumber());
                loanDetails1.setPan(customerDetails.getPanNumber());
                loanDetails1.setCustomerName(customerDetails.getCustomerName());
                loanDetails1.setAddressDetailsResidential(customerDetails.getAddressDetailsResidential());
                loanDetails1.setMobileNumber(customerDetails.getMobileNumber());

                loanDetailsRepository.save(loanDetails1);
                logger.info("customer details saved");


            } else {
                phoneNo = loanDetails.get().getMobileNumber();
            }
            return phoneNo;
        } catch (Exception e) {
            System.out.println(e);
            return phoneNo;

        }

    }


    @Override
    public LoanDetails otpValidation(String mobileNo, String otpCode, String loanNo) {

        OtpDetails otpDetails = otpDetailsRepository.IsotpExpired(mobileNo, otpCode);
        Duration duration = Duration.between(otpDetails.getOtpExprTime(), LocalDateTime.now());
        return (duration.toMinutes() > 50) ? null : loanDetailsRepository.getLoanDetail(loanNo).orElseThrow(() -> new RuntimeException("Loan not valid"));

    }


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
            if (!urls.isEmpty())
                documentDetail = callExtractionService(urls, inputBase64);
        } catch (Exception e) {

            documentDetail.put("code", "1111");
            documentDetail.put("msg", "Technical issue");
        }
        return documentDetail;
    }


    private HashMap<String, String> callExtractionService(List<String> urls, InputBase64 inputBase64) {
        HashMap<String, String> extractedDetails;

        if (inputBase64.getDocumentType().equals("aadhar")) {
            extractedDetails = singzyServices.extractAadharDetails(urls, inputBase64.getDocumentId());
        } else {
            extractedDetails = singzyServices.extractPanDetails(urls, inputBase64.getDocumentId());
        }

        if (extractedDetails.get("code").equals("0000")) urls.forEach(url -> {
            saveUpdatedDetails(inputBase64, url);
        });
        return extractedDetails;
    }

    /**
     *
     */
    @Override
    public CommonResponse updateCustomerKycFlag(String loanNo) {

        CommonResponse commonResponse = new CommonResponse();
        try {
                Optional<LoanDetails> loanDetails = loanDetailsRepository.getLoanDetail(loanNo);
                System.out.println(loanNo);
               customerRepository.updateKycFlag(loanDetails.get().getLoanNumber());
               loanDetailsRepository.deleteById(loanDetails.get().getUserId());
        } catch (Exception e) {
            commonResponse.setMsg("Loan is not valid, try again");
            commonResponse.setCode("1111");
        }
        return commonResponse;
    }


    @Override
    public CommonResponse callDdfsService(UpdateAddress inputAddress, String applicationNO, Long loanId) {
        CommonResponse commonResponse = new CommonResponse();

        List<DdfsUpload> ddfsUploads = ddfsUploadRepository.getImageUrl(inputAddress.getLoanNo());
        if(!ddfsUploads.isEmpty()) {

            for (DdfsUpload result : ddfsUploads) {
                String imageUrl = result.getImageUrl();
                try {
                    InputStream inputStream = new URL(imageUrl).openStream();

                    byte[] imageBytes = IOUtils.toByteArray(inputStream);

                    String base64String = Base64.encodeBase64String(imageBytes);
                    inputStream.close();
                    if (ddfsUtility.callDDFSApi(base64String, applicationNO)) {
                        result.setFileName(applicationNO);
                        result.setDdfsFlag("Y");
                        ddfsUploadRepository.save(result);
                    } else {
                        commonResponse.setCode("1111");
                        commonResponse.setMsg("File upload error, try again");
                        break;

                    }

                } catch (IOException e) {
                    System.out.println(e);
                    commonResponse.setCode("1111");
                    commonResponse.setMsg("File upload error, try again");

                }
            }
        }
        else
        {
            commonResponse.setCode("1111");
            commonResponse.setMsg("File upload error, try again");
            logger.info("file bucket url does not exist.");
        }
        return commonResponse;
    }

    public void saveUpdatedDetails(InputBase64 inputUpdatedDetails, String url) {
        DdfsUpload updatedDetails = new DdfsUpload();
        updatedDetails.setLoanNo(inputUpdatedDetails.getLoanNo());
        updatedDetails.setDocumentType(inputUpdatedDetails.getDocumentType());
        updatedDetails.setDdfsFlag("N");
        updatedDetails.setImageUrl(url);

        try {
            ddfsUploadRepository.save(updatedDetails);

        } catch (Exception e) {
            System.out.println(e);
        }
    }


    @Override
    public KycCountUpload kycCount() {

        try {
            KycCountUpload kycCount = new KycCountUpload();
            Integer existingCount = customerRepository.getKycCountDetail();
            Integer updatedCount = ddfsUploadRepository.getUpdatedCount();
            kycCount.setUpdatedKyc(updatedCount);
            kycCount.setExistingKyc(existingCount);

            return kycCount;

        } catch (Exception e) {
            throw new RuntimeException("failed" + e);
        }

    }

    @Override
    public LoanDetails loanDetails(String loanNo) {
        return loanDetailsRepository.getLoanDetail(loanNo).orElseThrow(() -> new RuntimeException("Loan not valid"));
    }


}
