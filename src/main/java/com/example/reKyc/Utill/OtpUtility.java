package com.example.reKyc.Utill;

import com.example.reKyc.Entity.OtpDetails;
import com.example.reKyc.Model.CustomerDataResponse;
import com.example.reKyc.Repository.OtpDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.HashMap;

@Service
public class OtpUtility {
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
    private OtpDetailsRepository otpDetailsRepository;
    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    public boolean generateOtp(String mobileNo, HashMap<String, String> otpResponse) {
        try {
            int count = otpDetailsRepository.countByMobile(mobileNo);

            if (count > 0) {
                otpDetailsRepository.deletePreviousOtp(mobileNo);

                logger.info("previous otp deleted");
            }
            int otpCode = (int) (Math.random() * 900000) + 100000;

            OtpDetails otpDetails = new OtpDetails();
            otpDetails.setOtpCode(Long.valueOf(otpCode));
            System.out.println(otpCode);
            otpDetails.setMobileNo(mobileNo);
            otpDetailsRepository.save(otpDetails);
            otpResponse.put("otpCode", String.valueOf(otpCode));
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public void sendTextMsg(String mobileNo, String body) {

        System.out.println(body);
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

    @Async
    public void sendOtp(String mobileNo, String otpCode, String loanNo) {
        String subStringLoanNo = loanNo.substring(loanNo.length() - 5, loanNo.length());
        String smsBody = "Dear Customer, Your Rekyc OTP is " + otpCode + " for Loan XXXXXXXXXXXXXXX" + subStringLoanNo + ".\\n\\nRegards\\nShubham Housing Development Finance Company";
        sendTextMsg(mobileNo, smsBody);
    }
}
