package com.example.reKyc.Model;

import lombok.Data;

@Data
public class UpdateAddress {
    private String loanNo;
    private  String documentId;
    private String documentType;
    private String updatedAddress;
    private String mobileNo;
    private String otpCode;
}
