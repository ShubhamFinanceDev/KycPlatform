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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
    @Autowired
    private AddressRepository addressRepository;
    @Value("https://api-preproduction.signzy.app/api/v3/getOkycOtp")
    private String getOkycOtpUrl;
    @Value("https://api-preproduction.signzy.app/api/v3/fetchOkycData")
    private String fetchOkycDataUrl;
    @Value("${authorization.key}")
    private String authorizationToken;

    private final RestTemplate restTemplate = new RestTemplate();
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
    public HashMap<String, String> callFileExchangeServices(InputBase64 inputBase64, CustomerDetails customerDetails) {

        HashMap<String, String> documentDetail = new HashMap<>();
        List<String> urls = new ArrayList<>();
        try {

            for (InputBase64.Base64Data base64 : inputBase64.getBase64Data()) {
                documentDetail = singzyServices.convertBase64ToUrl(base64.getFileType(), base64.getBase64String());
                if (documentDetail.containsKey("code")) break;
                urls.add(documentDetail.get("fileUrl"));
            }
            documentDetail = (!urls.isEmpty()) ? callExtractionService(urls, inputBase64) : documentDetail;
            if (documentDetail.containsKey("address")){
                customerDetails.setAddressDetailsResidential(documentDetail.get("address"));
                updateCustomerDetails(Optional.of(customerDetails),"Y");
            }
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
            updateCustomerDetails(loanDetails,"N");
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
        return loanDetailsRepository.getLoanDetail(loanNo).orElseThrow(null);
    }


    public void updateCustomerDetails(Optional<CustomerDetails> loanDetails,String status) {

        UpdatedDetails updatedDetails = new UpdatedDetails();
        updatedDetails.setAddressDetails(loanDetails.get().getAddressDetailsResidential());
        updatedDetails.setLoanNumber(loanDetails.get().getLoanNumber());
        updatedDetails.setRekycStatus(status);
        updatedDetails.setApplicationNumber(loanDetails.get().getApplicationNumber());
        updatedDetails.setRekycDate(Date.valueOf(LocalDate.now()));
        updatedDetails.setRekycDocument(loanDetails.get().getAadhar());
        updatedDetailRepository.save(updatedDetails);
    }

    public List<UpdatedDetails> getReportDataList(){
        try {
            return updatedDetailRepository.findAll();
        }catch (Exception e){
            System.out.println("Error while executing report query :" +e.getMessage());
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

    public void sendOtpOnContactLists(List<String> mobileList) {
        for (String mobileNo : mobileList) {
            otpUtility.sendTextMsg(mobileNo,SmsTemplate.lnkKyc);
        }
        logger.info("Rekyc link share to {}", mobileList.size()+" customer.");
    }



    @Override
    public Map<String, Object> getOkycOtp(String aadhaarNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("aadhaarNumber", aadhaarNumber);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody,headers);
        ResponseEntity response = restTemplate.exchange(getOkycOtpUrl, HttpMethod.POST, entity, HashMap.class);

        return (Map<String, Object>) response.getBody();
    }

    @Override
    public Map<String, Object> fetchOkycData(String otp, String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("otp", otp);
        requestBody.put("requestId", requestId);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity response = restTemplate.exchange(fetchOkycDataUrl, HttpMethod.POST, entity, HashMap.class);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        if (responseBody != null)
        {
            saveAddressData(responseBody);
        }
        return responseBody;
    }
    private void saveAddressData(Map<String, Object> responseBody) {
        Map<String, Object> addressData = (Map<String, Object>) responseBody.get("data");
        Map<String, Object> addressDetails = (Map<String, Object>) addressData.get("address");
        Address address = new Address();

        String dist = (String) addressDetails.get("dist");
        String state = (String) addressDetails.get("state");
        String po = (String) addressDetails.get("po");
        String loc = (String) addressDetails.get("loc");
        String vtc = (String) addressDetails.get("vtc");
        String subdist = (String) addressDetails.get("subdist");
        String street = (String) addressDetails.get("street");
        String house = (String) addressDetails.get("house");
        String landmark = (String) addressDetails.get("landmark");
        String pincode = (String) addressData.get("zip");

        // Build the full address by concatenating non-null and non-empty fields
        StringBuilder fullAddressBuilder = new StringBuilder();

        if (dist != null && !dist.isEmpty()) fullAddressBuilder.append(dist).append(", ");
        if (state != null && !state.isEmpty()) fullAddressBuilder.append(state).append(", ");
        if (po != null && !po.isEmpty()) fullAddressBuilder.append(po).append(", ");
        if (loc != null && !loc.isEmpty()) fullAddressBuilder.append(loc).append(", ");
        if (vtc != null && !vtc.isEmpty()) fullAddressBuilder.append(vtc).append(", ");
        if (subdist != null && !subdist.isEmpty()) fullAddressBuilder.append(subdist).append(", ");
        if (street != null && !street.isEmpty()) fullAddressBuilder.append(street).append(", ");
        if (house != null && !house.isEmpty()) fullAddressBuilder.append(house).append(", ");
        if (landmark != null && !landmark.isEmpty()) fullAddressBuilder.append(landmark).append(", ");
        if (pincode != null && !pincode.isEmpty()) fullAddressBuilder.append(pincode).append(", ");

        // Remove the last comma and space if any were appended
        if (fullAddressBuilder.length() > 0) {
            fullAddressBuilder.setLength(fullAddressBuilder.length() - 2);
        }

        String fullAddress = fullAddressBuilder.toString();

        address.setAddress(fullAddress);
        addressRepository.save(address);

    }
}
