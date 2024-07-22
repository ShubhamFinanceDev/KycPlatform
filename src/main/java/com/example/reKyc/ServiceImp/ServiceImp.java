package com.example.reKyc.ServiceImp;

import com.example.reKyc.Entity.*;
import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Repository.*;
import com.example.reKyc.Service.LoanNoAuthentication;
import com.example.reKyc.Utill.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;

@Service
public class ServiceImp implements com.example.reKyc.Service.Service {

    @Autowired
    private CustomerDetailsRepository customerDetailsRepository;
    @Autowired
    private LoanNoAuthentication loanNoAuthentication;
    @Autowired
    private KycCustomerRepository kycCustomerRepository;
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
    @Autowired
    private FetchingDetails fetchingDetails;

    Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    public HashMap<String, String> validateAndSendOtp(String loanNo) {
        logger.info("validating and sending OTP for loanNo: {}", loanNo);
        HashMap<String, String> otpResponse = new HashMap<>();

        try {
            String mobileNo;
            Optional<KycCustomer> customer = kycCustomerRepository.getCustomer(loanNo);
            List<CustomerDetails> customerDetailsList = fetchingDetails.getCustomerIdentification(loanNo).get().stream().filter(identification -> identification.getIdentificationType().contains("AAdhar_No")).collect(Collectors.toList());

            if (customer.isEmpty() && customerDetailsList.isEmpty()) {
                logger.warn("Loan number {} not found", loanNo);
                otpResponse.put("msg", "Loan no not found");
                otpResponse.put("code", "1111");
                return otpResponse;
            }

            mobileNo = customer.get().getMobileNo();
            if (mobileNo != null && !mobileNo.isEmpty()) {
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
    public boolean otpValidation(String mobileNo, String otpCode, String loanNo) {
        logger.info("Performing OTP validation for loanNo: {}, mobileNo: {}, otpCode: {}", loanNo, mobileNo, otpCode);

        OtpDetails otpDetails = otpDetailsRepository.isOtpValid(mobileNo, otpCode);
        if (otpDetails == null) {
            logger.warn("Invalid OTP for mobileNo: {}", mobileNo);
            return false;
        }
        if (Duration.between(otpDetails.getOtpExprTime(), LocalDateTime.now()).toMinutes() > 10) {
            logger.warn("OTP expired for mobileNo: {}", mobileNo);
            return false;
        }

        return true;
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
//            if (documentDetail.containsKey("address")){
//                customerDetails.setResidentialAddress(documentDetail.get("address"));
//                updateCustomerDetails(Optional.of(customerDetails),null);
//            }
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
            Optional<CustomerDetails> loanDetails = customerDetailsRepository.getLoanDetail(loanNo);
            kycCustomerRepository.updateKycFlag(loanDetails.get().getLoanAccountNo());
            updateCustomerDetails(loanDetails, "N");
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
    public CommonResponse callDdfsService(UpdateAddress inputAddress, String loanNo) {
        logger.info("Calling ddfs service for applicationNO: {}", loanNo);
        CommonResponse commonResponse = new CommonResponse();
        try {
            CustomerDataResponse customerDataResponse = fetchingDetails.getCustomerData(loanNo).get();

            List<DdfsUpload> ddfsUploads = ddfsUploadRepository.getImageUrl(inputAddress.getLoanNo());
            if (!ddfsUploads.isEmpty() && customerDataResponse!=null) {

                for (DdfsUpload result : ddfsUploads) {
                    String imageUrl = result.getImageUrl();
                    try {

                        InputStream inputStream = new URL(imageUrl).openStream();

                        byte[] imageBytes = IOUtils.toByteArray(inputStream);

                        String base64String = Base64.encodeBase64String(imageBytes);
                        inputStream.close();
                        if (ddfsUtility.callDDFSApi(base64String, customerDataResponse.getApplicationNumber())) {
                            result.setFileName(customerDataResponse.getApplicationNumber());
                            result.setDdfsFlag("Y");
                            ddfsUploadRepository.save(result);
                        } else {
                            commonResponse.setCode("1111");
                            commonResponse.setMsg("File upload error, try again");
                            break;

                        }
                        logger.info("DDFS service call successfully for applicationNO: {}", customerDataResponse.getApplicationNumber());

                    } catch (Exception e) {
                        logger.error("Error occurred while calling ddfs service for applicationNO: {}", customerDataResponse.getApplicationNumber(), e);
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
                updatedDetailRepository.updateKycStatus(customerDataResponse.getLoanNumber());
                otpUtility.sendTextMsg(inputAddress.getMobileNo(), SmsTemplate.updationKyc);
            }
        } catch (Exception e) {
            logger.info("Error while fetching customer data for loanNo: {}", loanNo);
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
            Integer existingCount = updatedDetailRepository.getKycCountDetail();
            Integer updatedCount = updatedDetailRepository.getUpdatedCount();
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
        return customerDetailsRepository.getLoanDetail(loanNo).orElseThrow(null);
    }


    public void updateCustomerDetails(Optional<CustomerDetails> loanDetails, String status) {

        UpdatedDetails updatedDetails = new UpdatedDetails();
        updatedDetails.setAddressDetails(loanDetails.get().getResidentialAddress());
        updatedDetails.setLoanNumber(loanDetails.get().getLoanAccountNo());
        updatedDetails.setRekycStatus(status);
        updatedDetails.setApplicationNumber(loanDetails.get().getApplicationNumber());
        updatedDetails.setRekycDate(Date.valueOf(LocalDate.now()));
        updatedDetails.setRekycDocument(loanDetails.get().getIdentificationNumber());
        updatedDetailRepository.save(updatedDetails);
    }

    public List<UpdatedDetails> getReportDataList() {
        try {
            return updatedDetailRepository.latestKycDetail();
        } catch (Exception e) {
            System.out.println("Error while executing report query :" + e.getMessage());
        }
        return null;
    }



    public void generateExcel(HttpServletResponse response, List<UpdatedDetails> reportList) {

        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Updated-Details");
            int rowCount = 0;

            String[] header = {"Application Number", "Rekyc Date", "Address Details", "ReKYC Mode", "Loan Number", "Rekyc Document"};
            Row headerRow = sheet.createRow(rowCount++);
            int cellCount = 0;

            for (String headerValue : header) {
                headerRow.createCell(cellCount++).setCellValue(headerValue);
            }
            for (UpdatedDetails list : reportList) {
                Row row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(list.getApplicationNumber());
                row.createCell(1).setCellValue(list.getRekycDate().toString());
                row.createCell(2).setCellValue(list.getAddressDetails());
                row.createCell(3).setCellValue(list.getRekycStatus());
                row.createCell(4).setCellValue(list.getLoanNumber());
                row.createCell(5).setCellValue("Aadhaar");
            }
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=MIS_Report.xlsx");

            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            System.out.println("Error while executing report query :" + e.getMessage());
        }
    }

    public CommonResponse sendSmsOnMobile() {
        CommonResponse commonResponse = new CommonResponse();
        try {
            List<String> mobileNo = kycCustomerRepository.findMobileNo();
            if (!mobileNo.isEmpty()) {
                for (String mobileList : mobileNo) {
                    otpUtility.sendTextMsg(mobileList, SmsTemplate.lnkKyc);
                    kycCustomerRepository.updateSmsSentFlag(mobileList);
                }
                commonResponse.setCode("0000");
                commonResponse.setMsg("Sms sent successfully for " + mobileNo.size() + " loan number");
                logger.info("Rekyc link shared with {} customers.", mobileNo.size());
            } else {
                commonResponse.setCode("1111");
                commonResponse.setMsg("No record found for send SMS");
            }
        } catch (Exception e) {
            logger.error("Exception in sendOtpOnMobile", e);
            commonResponse.setCode("1111");
            commonResponse.setMsg("Technical issue: " + e.getMessage());
        }
        return commonResponse;
    }
}
