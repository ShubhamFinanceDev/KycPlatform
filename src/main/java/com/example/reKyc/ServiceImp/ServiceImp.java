package com.example.reKyc.ServiceImp;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Entity.OtpDetails;
import com.example.reKyc.Entity.UpdatedDetails;
import com.example.reKyc.Model.*;
import com.example.reKyc.Repository.CustomerDetailsRepository;
import com.example.reKyc.Repository.OtpDetailsRepository;
import com.example.reKyc.Repository.UpdatedDetailsRepository;
import com.example.reKyc.Utill.MaskDocumentAndFile;
import com.example.reKyc.Utill.DateTimeUtility;
import com.example.reKyc.Utill.ExternalApiServices;
import com.example.reKyc.Utill.OtpUtility;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Service
public class ServiceImp implements com.example.reKyc.Service.Service {

    @Autowired
    private CustomerDetailsRepository customerDetailsRepository;
    @Autowired
    private OtpDetailsRepository otpDetailsRepository;
    @Autowired
    private UpdatedDetailsRepository updatedDetailsRepository;
    @Autowired
    private OtpUtility otpUtility;
    @Autowired
    private DateTimeUtility dateTimeUtility;
    @Autowired
    private MaskDocumentAndFile authToken;
    @Autowired
    private ExternalApiServices singzyServices;
    @Autowired
    ExternalApiServices externalApiServices;


    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    public HashMap validateAndSendOtp(String loanNo) {
        HashMap<String, String> otpResponse = new HashMap<>();
        CustomerDetails customerDetails = new CustomerDetails();
        try {


            customerDetails = customerDetailsRepository.findByLoanNumber(loanNo);
            if (customerDetails != null) {
                int otpCode = otpUtility.generateOtp(customerDetails);

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
                    otpResponse.put("msg", "Technical issue");
                    otpResponse.put("code", "1111");

                }

            } else {
                otpResponse.put("msg", "Loan no not found");
                otpResponse.put("code", "1111");

            }
            return otpResponse;
        } catch (Exception e) {
            System.out.println(e);
            otpResponse.put("msg", "Technical issue");
            otpResponse.put("code", "1111");
            return otpResponse;
        }
    }

    /**
     * @param mobileNo
     * @return
     */
    @Override
    public CustomerDetails getCustomerDetail(String mobileNo, String otpCode,String loanNo) {

        CustomerDetails customerDetails = new CustomerDetails();
        try {
            OtpDetails otpDetails = otpDetailsRepository.IsotpExpired(mobileNo, otpCode);
            if (otpDetails != null) {
                Duration duration = Duration.between(otpDetails.getOtpExprTime(), LocalDateTime.now());
                customerDetails = (duration.toMinutes() > 50) ? customerDetails : customerDetailsRepository.findUserDetailByMobile(mobileNo,loanNo);
            }

        } catch (Exception e) {
            System.out.println(e);
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


    public CustomerDetails checkExtractedDocumentId(String loanNo, String documentId, String documentType) {
        CustomerDetails customerDetails = new CustomerDetails();
        try {
            if (documentType.equals("aadhar")) {

                customerDetails = customerDetailsRepository.checkCustomerAadharNo(loanNo, documentId);

            }
            if (documentType.equals("pan")) {

                customerDetails = customerDetailsRepository.checkCustomerPanNo(loanNo, documentId);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return customerDetails;

    }


    public boolean saveUpdatedDetails(UpdateAddress inputUpdateAddress) {
        UpdatedDetails updatedDetails = new UpdatedDetails();
//        updatedDetails.setUpdatedAddress(inputUpdateAddress.getUpdatedAddress());
        updatedDetails.setMobileNo(inputUpdateAddress.getMobileNo());
        updatedDetails.setLoanNo(inputUpdateAddress.getLoanNo());
        updatedDetails.setDocumentId(inputUpdateAddress.getDocumentId());
        updatedDetails.setDocumentType(inputUpdateAddress.getDocumentType());

        try {
            updatedDetailsRepository.save(updatedDetails);
            return true;

        } catch (Exception e) {
            return false;

        }
    }

    /**
     * @param file
     * @return
     */
    @Override
    public String enableProcessFlag(MultipartFile file) {

        String errorMsg = "";
//        Integer enable;
        List<String> loanNo = new ArrayList<>();
        try {


            InputStream inputStream = file.getInputStream();
            ZipSecureFile.setMinInflateRatio(0);                //for zip bomb detected
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.next();

            if (headerRow.getCell(0).toString().equals("Loan-No")) {

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Cell cell = row.getCell(0);
                    errorMsg = (cell == null || cell.getCellType() == CellType.BLANK) ? "File upload error due to row no " + (row.getRowNum() + 1) + " is empty" : "";

                    if (errorMsg.isEmpty()) {
                        loanNo.add(cell.toString());
                    } else {
                        break;
                    }
                }
                if (!loanNo.isEmpty() && errorMsg.isEmpty()) {
                    customerDetailsRepository.enableKycFlag(loanNo);
                    errorMsg = "Successfully process.";
                }
            } else {
                errorMsg = "File format is not matching";
            }

        } catch (Exception e) {
            errorMsg = "failure:" + e;
        }
        return errorMsg;
    }

    /**
     * @param loanNo
     * @return
     */
    @Override
    public CustomerDetails checkLoanNo(String loanNo) {

        return this.customerDetailsRepository.findByLoanNumber(loanNo);
    }

    /**
     * @param loanNo
     * @return
     */
    @Override
    public CommonResponse updatCustomerKycFlag(String loanNo) {

        CommonResponse commonResponse=new CommonResponse();
        try {
            CustomerDetails customerDetails=customerDetailsRepository.findByLoanNumber(loanNo);
            if (customerDetails !=null)
            {
                try {

                    customerDetailsRepository.updateKycFlag(loanNo);
                    commonResponse.setMsg("Successfully");
                    commonResponse.setCode("0000");
                }
                catch (Exception e)
                {
                    commonResponse.setMsg("Flag did not updated.");
                    commonResponse.setCode("1111");
                }
            }

        }
        catch (Exception e)
        {
            commonResponse.setMsg("Loan is not valid, try again");
            commonResponse.setCode("1111");
        }
        return commonResponse;
    }
}
