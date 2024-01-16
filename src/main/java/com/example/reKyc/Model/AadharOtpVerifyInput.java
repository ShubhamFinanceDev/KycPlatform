package com.example.reKyc.Model;

import lombok.Data;

@Data
public class AadharOtpVerifyInput {

    private String requestID ;
    private String otpCode;

}
