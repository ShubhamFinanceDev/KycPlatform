package com.example.reKyc.Utill;

import com.example.reKyc.Entity.KycCustomer;
import com.example.reKyc.Entity.OtpDetails;
import com.example.reKyc.Repository.KycCustomerRepository;
import com.example.reKyc.Repository.OtpDetailsRepository;
import jakarta.mail.internet.MimeMessage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class SmsUtility {
    @Value("${otp.url}")
    private String otpUrl;
    @Value("${otp.method}")
    private String otpMethod;
    @Value("${otp.key}")
    private String otpKey;
    @Value("${otp.format}")
    private String otpFormat;
    @Value("${otp.sender}")
    private String otpSender;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${spring.mail.reciver}")
    private String receiver;


    @Autowired
    private OtpDetailsRepository otpDetailsRepository;

    @Autowired
    private KycCustomerRepository kycCustomerRepository;
    private final Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    public void generateOtp(String mobileNo, HashMap<String, String> otpResponse) {
        try {
            int count = otpDetailsRepository.countByMobile(mobileNo);

            if (count > 0) {
                otpDetailsRepository.deletePreviousOtp(mobileNo);

                logger.info("previous otp deleted");
            }
            int otpCode = ((int) (Math.random() * 900000) + 100000);

            OtpDetails otpDetails = new OtpDetails();
            otpDetails.setOtpCode(Long.valueOf(otpCode));
            System.out.println(otpCode);
            otpDetails.setMobileNo(mobileNo);
            otpDetailsRepository.save(otpDetails);
            otpResponse.put("otpCode", String.valueOf(otpCode));
        } catch (Exception e) {
            otpResponse.put("msg", "Please try again.");
            otpResponse.put("code", "1111");
            logger.error("Error while generating otp.");
        }
    }

    @Async
    public void sendTextMsg(String mobileNo, String body) {

        String apiUrl = otpUrl + "?method=" + otpMethod + "&api_key=" + otpKey + "&to=" + mobileNo + "&sender=" + otpSender + "&message=" + body + "&format=" + otpFormat + "&unicode=auto";
        try {
            RestTemplate restTemplate = new RestTemplate();
            HashMap<String, String> otpResponse = restTemplate.getForObject(apiUrl, HashMap.class);

            if (otpResponse.get("status").equals("OK")) {
                logger.info("Otp send successfully");
            }
        } catch (Exception e) {
            logger.error("Error while sending otp on mobile.");
        }
    }

    public HashMap<String, String> sendOtp(String mobileNo, String otpCode, String loanNo) {
        String subStringLoanNo = loanNo.substring(loanNo.length() - 5, loanNo.length());
        String smsBody = "Dear Customer, Your Rekyc OTP is " + otpCode + " for Loan XXXXXXXXXXXXXXX" + subStringLoanNo + ".\\n\\nRegards\\nShubham Housing Development Finance Company";
//        sendTextMsg(mobileNo, smsBody);
        HashMap<String, String> otpResponse = new HashMap<>();
        otpResponse.put("mobile", mobileNo);
        otpResponse.put("msg", "Otp send successfully.");
        otpResponse.put("code", "0000");
        logger.info("otp sent on mobile");
        return otpResponse;
    }

    @Scheduled(cron = "2 * * * * *")
    public void reminderSmsOnMobileNo() {
        try {
            List<KycCustomer> kycCustomers = kycCustomerRepository.findMobileNumber();
            if (!kycCustomers.isEmpty()) {
                byte[] excelData = generateFile(kycCustomers);
//
                sendSimpleMail(excelData);
                logger.info("KYC report shared with {} customers.", kycCustomers.size());
            } else {
                logger.info("No eligible mobile numbers found to send SMS notifications");
            }
        } catch (Exception e) {
            logger.info("Technical issue: " + e.getMessage());
        }
    }

    public byte[] generateFile(List<KycCustomer> reportModels) throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("SMS-Report");
        int rowCount = 0;
        String[] header = {"loan_number", "kyc_flag", "mobile_no", "sms_flag"};
        Row headerRow = sheet.createRow(rowCount++);
        int cellCount = 0;

        for (String headerValue : header) {
            headerRow.createCell(cellCount++).setCellValue(headerValue);
        }
        for (KycCustomer readData : reportModels) {
            Row row = sheet.createRow(rowCount++);
            cellCount = 0;
            row.createCell(cellCount++).setCellValue(readData.getLoanNumber());
            row.createCell(cellCount++).setCellValue(readData.getMobileNo());
            row.createCell(cellCount++).setCellValue(readData.getKycFlag());
            row.createCell(cellCount++).setCellValue(readData.getSmsFlag());
        }
        logger.info("No of records insert in file " + rowCount);
        byte[] excelData;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            excelData = outputStream.toByteArray();
        } finally {
            workbook.close();
        }
        logger.info("File have been created");
        return excelData;
    }

    public void sendSimpleMail(byte[] excelData) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(sender);
            helper.setTo(receiver);
            helper.setText("Msg send successfully");
            helper.setSubject("Information mail");

            InputStreamSource attachmentSource = new ByteArrayResource(excelData);
            helper.addAttachment("Reminder sms report.xlsx", attachmentSource);
            javaMailSender.send(message);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

