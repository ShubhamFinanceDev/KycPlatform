package com.example.reKyc.Utill;

import com.example.reKyc.Model.CustomerDataResponse;
import com.example.reKyc.Repository.OtpDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public int generateOtp(String mobileNo)
    {
        try {
            int count = otpDetailsRepository.countByMobile(mobileNo);

            if (count > 0) {
                otpDetailsRepository.deletePreviousOtp(mobileNo);
                logger.info("previous otp deleted");
            }
            int randomNo = (int) (Math.random() * 900000) + 100000;
            return randomNo;
        }
        catch (Exception e)
        {
            return 0;
        }
    }


    public void sendTextMsg(String mobileNo, String body) {

        String apiUrl = otpUrl + "?method=" + otpMethod + "&api_key=" + otpKey + "&to=" + mobileNo + "&sender=" + otpSender + "&message=" + body + "&format=" + otpFormat;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HashMap<String, String> otpResponse = restTemplate.getForObject(apiUrl, HashMap.class);

            if (otpResponse.get("status").equals("OK")) {
                System.out.println("Sms send successfully");
            }
        } catch (Exception e) {
            System.out.println("error while sending sms");
        }
    }


    public boolean sendOtp(String mobileNo, int otpCode,String loanNo)
    {
            String subStringLoanNo=loanNo.substring(loanNo.length()-5,loanNo.length());
            String smsBody ="Your E-Nach Registration OTP is "+otpCode+" for Loan XXXXXXXXXXXXXX"+subStringLoanNo+".\n" +
                        "Regards\n" +
                        "Shubham Housing Development Finance Company";
            try
            {
                sendTextMsg(mobileNo,smsBody);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }

    }
}
