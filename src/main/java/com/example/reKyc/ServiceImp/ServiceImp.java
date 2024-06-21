package com.example.reKyc.ServiceImp;

import com.example.reKyc.Entity.*;
import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Repository.*;
import com.example.reKyc.Service.LoanNoAuthentication;
import com.example.reKyc.Utill.*;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
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
    private AadharAndPanUtility externalApiServices;
    @Autowired
    private DdfsUtility ddfsUtility;
    @Autowired
    private UpdatedDetailRepository updatedDetailRepository;

    Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    public HashMap<String, String> validateAndSendOtp(String loanNo) {
        logger.info("validating and sending OTP for loanNo: {}", loanNo);
        HashMap<String, String> otpResponse = new HashMap<>();

        try {
            String mobileNo;
            Optional<KycCustomer> customer = customerRepository.getCustomer(loanNo);
            if (customer.isEmpty()) {
                logger.warn("Loan number {} not found", loanNo);
                otpResponse.put("msg", "Loan no not found");
                otpResponse.put("code", "1111");
                return otpResponse;
            }
            CustomerDetails customerDetails =loanDetailsRepository.getLoanDetails(loanNo);
            mobileNo=(customerDetails !=null) ? customerDetails.getMobileNumber() : loanNoAuthentication.getCustomerData(loanNo);
            if (mobileNo !=null && !mobileNo.isEmpty()) {
                otpUtility.generateOtp(mobileNo, otpResponse);
                if (!otpResponse.containsKey("otpCode")) {
                    return otpResponse;
                }
                return otpUtility.sendOtp(mobileNo, otpResponse.get("otpCode"), loanNo);
            } else {
                logger.warn("Failed to send OTP for loanNo: {}", loanNo);
                otpResponse.put("msg", "Please try again");
                otpResponse.put("code", "1111");
            }
        } catch (Exception e) {
            logger.error("Error while sending OTP for loanNo: {}", loanNo, e);
            otpResponse.put("msg", "Technical issue.");
            otpResponse.put("code", "1111");
        }
        return otpResponse;
    }


    @Override
    public CustomerDetails otpValidation(String mobileNo, String otpCode, String loanNo) {
        logger.info("Performing OTP validation for loanNo: {}, mobileNo: {}, otpCode: {}", loanNo, mobileNo, otpCode);

        OtpDetails otpDetails = otpDetailsRepository.isOtpValid(mobileNo, otpCode);
        if (otpDetails == null) {
            logger.warn("Invalid OTP for mobileNo: {}", mobileNo);
            return null;
        }
        if (Duration.between(otpDetails.getOtpExprTime(), LocalDateTime.now()).toMinutes() > 10) {
            logger.warn("OTP expired for mobileNo: {}", mobileNo);
            return null;
        }

        return loanDetailsRepository.getLoanDetail(loanNo).orElse(null);
    }


    @Override
    public HashMap<String, String> callFileExchangeServices(InputBase64 inputBase64) {

        HashMap<String, String> documentDetail = new HashMap<>();
        List<String> urls = new ArrayList<>();
        try {

            for (InputBase64.Base64Data base64 : inputBase64.getBase64Data()) {
                documentDetail = singzyServices.convertBase64ToUrl(base64.getFileType(), base64.getBase64String());
                if (documentDetail.containsKey("code")) break;
                urls.add(documentDetail.get("fileUrl"));
            }
            documentDetail = (!urls.isEmpty()) ? callExtractionService(urls, inputBase64) : documentDetail;
        } catch (Exception e) {

            documentDetail.put("code", "1111");
            documentDetail.put("msg", "Technical issue");
        }
        return documentDetail;
    }


    private HashMap<String, String> callExtractionService(List<String> urls, InputBase64 inputBase64) {
        HashMap<String, String> extractedDetails;
        extractedDetails = (inputBase64.getDocumentType().equals("aadhar") ? singzyServices.extractAadharDetails(urls, inputBase64.getDocumentId()) : singzyServices.extractPanDetails(urls, inputBase64.getDocumentId()));

        if (extractedDetails.get("code").equals("0000")) {
            deleteUnProcessRecord(inputBase64.getLoanNo());
            urls.forEach(url -> {
                saveUpdatedDetails(inputBase64, url);

            });
        }
        return extractedDetails;
    }

    /**
     *
     */
    @Override
    public CommonResponse updateCustomerKycFlag(String loanNo, String mobileNo) {

        logger.info("Updating customer KYC flag for loanNo: {}", loanNo);
        CommonResponse commonResponse = new CommonResponse();
        try {
            Optional<CustomerDetails> loanDetails = loanDetailsRepository.getLoanDetail(loanNo);
            customerRepository.updateKycFlag(loanDetails.get().getLoanNumber());
            updateCustomerDetails(loanDetails);
            loanDetailsRepository.deleteById(loanDetails.get().getUserId());
            otpUtility.sendTextMsg(mobileNo, SmsTemplate.existingKyc); //otp send
            logger.info("Customer KYC flag updated successfully for loanNo: {}", loanNo);

        } catch (Exception e) {
            logger.error("Error occurred while updating customer KYC flag for loanNo: {}", loanNo, e);
            commonResponse.setMsg("Loan is not valid, try again");
            commonResponse.setCode("1111");
        }
        return commonResponse;
    }


    @Override
    public CommonResponse callDdfsService(UpdateAddress inputAddress, CustomerDetails customerDetails) {
        logger.info("Calling ddfs service for applicationNO: {}", customerDetails.getApplicationNumber());
        CommonResponse commonResponse = new CommonResponse();

        List<DdfsUpload> ddfsUploads = ddfsUploadRepository.getImageUrl(inputAddress.getLoanNo());
        if (!ddfsUploads.isEmpty()) {

            for (DdfsUpload result : ddfsUploads) {
                String imageUrl = result.getImageUrl();
                try {
                    InputStream inputStream = new URL(imageUrl).openStream();

                    byte[] imageBytes = IOUtils.toByteArray(inputStream);

                    String base64String = Base64.encodeBase64String(imageBytes);
                    inputStream.close();
                    if (ddfsUtility.callDDFSApi(base64String, customerDetails.getApplicationNumber())) {
                        result.setFileName(customerDetails.getApplicationNumber());
                        result.setDdfsFlag("Y");
                        ddfsUploadRepository.save(result);
                    } else {
                        commonResponse.setCode("1111");
                        commonResponse.setMsg("File upload error, try again");
                        break;

                    }
                    logger.info("DDFS service call successfully for applicationNO: {}", customerDetails.getApplicationNumber());

                } catch (IOException e) {
                    logger.error("Error occurred while calling ddfs service for applicationNO: {}", customerDetails.getApplicationNumber(), e);
                    System.out.println(e);
                    commonResponse.setCode("1111");
                    commonResponse.setMsg("File upload error, try again");
                    break;

                }
            }
        } else {
            commonResponse.setCode("1111");
            commonResponse.setMsg("File upload error, try again");
            logger.info("file bucket url does not exist.");
        }
        if (commonResponse.getCode().equals("0000")) {
            otpUtility.sendTextMsg(inputAddress.getMobileNo(), SmsTemplate.updationKyc);
            updateCustomerDetails(Optional.of(customerDetails));
            loanDetailsRepository.deleteById(customerDetails.getUserId());

        }
        return commonResponse;
    }

    public void deleteUnProcessRecord(String loanNo) {
        logger.info("Deleting unprocessed record for loanNo: {}", loanNo);
        ddfsUploadRepository.deletePreviousDetail(loanNo).forEach(data -> {
            ddfsUploadRepository.deleteById(data.getUpdatedId());
        });
    }

    public void saveUpdatedDetails(InputBase64 inputUpdatedDetails, String url) {
        logger.info("saving updated details for url: {}", inputUpdatedDetails.getLoanNo());
        DdfsUpload updatedDetails = new DdfsUpload();

        try {

            updatedDetails.setLoanNo(inputUpdatedDetails.getLoanNo());
            updatedDetails.setDocumentType(inputUpdatedDetails.getDocumentType());
            updatedDetails.setDdfsFlag("N");
            updatedDetails.setImageUrl(url);
            ddfsUploadRepository.save(updatedDetails);
            logger.info("Updated details saved successfully for loanNo: {}", inputUpdatedDetails.getLoanNo());

        } catch (Exception e) {
            logger.error("Error occurred while saving updated details for loanNo: {}", inputUpdatedDetails.getLoanNo(), e);
            System.out.println(e);
        }
    }


    @Override
    public KycCountUpload kycCount() {
        logger.info("Fetching KYC count");

        try {
            KycCountUpload kycCount = new KycCountUpload();
            Integer existingCount = customerRepository.getKycCountDetail();
            Integer updatedCount = ddfsUploadRepository.getUpdatedCount();
            kycCount.setUpdatedKyc(updatedCount);
            kycCount.setExistingKyc(existingCount);
            logger.info("KYC count fetched successfully: Existing KYC count: {}, Updated KYC count: {}", existingCount, updatedCount);

            return kycCount;

        } catch (Exception e) {
            logger.error("Error occurred while fetching KYC count", e);
            throw new RuntimeException("failed" + e);
        }

    }

    @Override
    public CustomerDetails loanDetails(String loanNo) {
        logger.info("Fetching loan details for loanNo: {}", loanNo);
        return loanDetailsRepository.getLoanDetail(loanNo).orElseThrow(null);
    }

    public void updateCustomerDetails(Optional<CustomerDetails> loanDetails) {
        UpdatedDetails updatedDetails = new UpdatedDetails();
        updatedDetails.setAddressDetails(loanDetails.get().getAddressDetailsResidential());
        updatedDetails.setLoanNumber(loanDetails.get().getLoanNumber());
        updatedDetails.setApplicationNumber(loanDetails.get().getApplicationNumber());
        updatedDetails.setRekycDate(Date.valueOf(LocalDate.now()));
        updatedDetails.setRekycStatus("N");
        updatedDetails.setRekycDocument(loanDetails.get().getAadhar());
        updatedDetailRepository.save(updatedDetails);
    }
}
