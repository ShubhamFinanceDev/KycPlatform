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

    public int generateOtp(CustomerDataResponse customerDetails)
    {
        try {
            int count = otpDetailsRepository.countByMobile(customerDetails.getMobileNumber());

            if (count > 0) {
                otpDetailsRepository.deletePreviousOtp(customerDetails.getMobileNumber());
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


    public void sendOtp(String mobileNo, String status)
    {
        String smsBody = null;
        if (status.equals("upToDate"))
        {
            smsBody=SmsTemplate.existingKyc;
        } else {
            if (status.equals("update")) {
                smsBody = SmsTemplate.updationKyc;
            }
        }

        System.out.println("sms case="+status);

        String apiUrl=otpUrl+"?method="+otpMethod+"&api_key="+otpKey+"&to="+mobileNo+"&sender="+otpSender+"&message="+smsBody+"&format="+otpFormat;

        RestTemplate restTemplate=new RestTemplate();
        HashMap<String,String> otpResponse=restTemplate.getForObject(apiUrl,HashMap.class);

        if(otpResponse.get("status").equals("OK"))
        {
            System.out.println("Sms send successfully");
        }
    }


    public boolean sendOtp(String mobileNo, int otpCode,String loanNo)
    {
        boolean status=false;

            String smsBody ="Your E-Nach Registration OTP is "+otpCode+" for Loan XXXXXXXXXXXXXX"+loanNo+".\n" +
                        "Regards\n" +
                        "Shubham Housing Development Finance Company";

        String apiUrl=otpUrl+"?method="+otpMethod+"&api_key="+otpKey+"&to="+mobileNo+"&sender="+otpSender+"&message="+smsBody+"&format="+otpFormat;
        RestTemplate restTemplate=new RestTemplate();
        HashMap<String,String> otpResponse=restTemplate.getForObject(apiUrl,HashMap.class);

        if(otpResponse.get("status").equals("OK"))
        {
            status=true;
            System.out.println("Sms send successfully");
        }
        return status;
    }
}
