package com.example.reKyc.Model;

import lombok.Data;

@Data
public class OkycDataRequest
{
    private String requestId;
    private String otp;
    private String loanNumber;
}
