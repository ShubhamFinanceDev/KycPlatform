package com.example.reKyc.Model;


import lombok.Data;

@Data
public class OtpVerification {

    private String otpId;
    private String otpCode;
    private String mobileNo;
}
