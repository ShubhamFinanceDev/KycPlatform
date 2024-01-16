package com.example.reKyc.Model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtRequest {
    private String mobileNo;
    private String otpCode;
}
