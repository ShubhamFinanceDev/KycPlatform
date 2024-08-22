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
    private SmsUtility otpUtility;
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
                if(customer.isPresent()) {
                    List<CustomerDetails> customerDetailsList = fetchingDetails.getCustomerIdentification(loanNo).get().stream().filter(identification -> identification.getIdentificationType().contains("AAdhar_No")).collect(Collectors.toList());
                    if(customerDetailsList.isEmpty()) {
                        logger.warn("Identification type did not found for Loan number {}", loanNo);
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
                }
                else {
                    logger.warn("Loan number {} not found", loanNo);
                    otpResponse.put("msg", "Loan no not found");
                    otpResponse.put("code", "1111");
                    return otpResponse;
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
    public HashMap<String, String> callFileExchangeServices(InputBase64 inputBase64, CustomerDataResponse customerDataResponse) {

        HashMap<String, String> documentDetail = new HashMap<>();
        List<String> urls = new ArrayList<>();
        try {

            for (InputBase64.Base64Data base64 : inputBase64.getBase64Data()) {
                documentDetail = singzyServices.convertBase64ToUrl(base64.getFileType(), base64.getBase64String());
                if (documentDetail.containsKey("code")) break;
                urls.add(documentDetail.get("fileUrl"));
            }
            documentDetail = (!urls.isEmpty()) ? callExtractionService(urls, inputBase64,customerDataResponse) : documentDetail;
        } catch (Exception e) {

            documentDetail.put("code", "1111");
            documentDetail.put("msg", "Technical issue");
        }
        return documentDetail;
    }


    private HashMap<String, String> callExtractionService(List<String> urls, InputBase64 inputBase64, CustomerDataResponse customerDataResponse) {
        HashMap<String, String> extractedDetails = new HashMap<>();
        String documentType = inputBase64.getDocumentType();

        try {
            switch (documentType) {
                case "aadhar":
                    extractedDetails = singzyServices.extractAadharDetails(urls, inputBase64.getDocumentId());
                    break;

                case "pan":
                    extractedDetails = singzyServices.extractPanDetails(urls, inputBase64.getDocumentId(),customerDataResponse);
                    break;
                default:
            }
            if ("0000".equals(extractedDetails.get("code"))) {
                deleteUnProcessRecord(inputBase64.getLoanNo());
                urls.forEach(url -> saveUpdatedDetails(inputBase64, url));
            }

        } catch (Exception e) {
            extractedDetails.put("code", "1111");
            extractedDetails.put("msg", "Technical issue, please try again");
            logger.error("Error extracting details: {}", e.getMessage());

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
            CustomerDataResponse customerDataResponse = fetchingDetails.getCustomerData(loanNo).get();
            kycCustomerRepository.updateExistingKycFlag(customerDataResponse.getLoanNumber());
            updateCustomerDetails(customerDataResponse, "N","aadhar");
            otpUtility.sendTextMsg(customerDataResponse.getPhoneNumber(), SmsTemplate.existingKyc); //otp send
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
            if (!ddfsUploads.isEmpty() && customerDataResponse.getApplicationNumber() != null) {

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
        } catch (Exception e) {
            commonResponse.setCode("1111");
            commonResponse.setMsg("Technical error, try again");
            logger.info("Error while fetching customer data for loanNo: {}", loanNo);
        }
        return commonResponse;

    }
@Override
    public void confirmationSmsAndUpdateKycStatus(String loanNo, String mobileNo) throws Exception {

        updatedDetailRepository.updateKycStatus(loanNo);
        otpUtility.sendTextMsg(mobileNo, SmsTemplate.updationKyc);
        kycCustomerRepository.kycCustomerUpdate(loanNo);

    }


    public void deleteUnProcessRecord(String loanNo) {
        logger.info("Deleting unprocessed record for loanNo: {}", loanNo);
        ddfsUploadRepository.deletePreviousDetail(loanNo).forEach(data -> {
            ddfsUploadRepository.deleteById(data.getUpdatedId());
        });
    }


    public void saveUpdatedDetails(InputBase64 inputUpdatedDetails, String url) {
        logger.info("saving updated details for loanNo: {}", inputUpdatedDetails.getLoanNo());
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


    public void updateCustomerDetails(CustomerDataResponse loanDetails, String status,String documentType) {

        UpdatedDetails updatedDetails = new UpdatedDetails();
        updatedDetails.setLoanNumber(loanDetails.getLoanNumber());
        updatedDetails.setAddressDetails(loanDetails.getAddressDetailsResidential());
        updatedDetails.setRekycStatus(status);
        updatedDetails.setApplicationNumber(loanDetails.getApplicationNumber());
        updatedDetails.setRekycDate(Date.valueOf(LocalDate.now()));
        updatedDetails.setRekycDocument(documentType);
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
