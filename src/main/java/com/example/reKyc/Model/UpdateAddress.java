package com.example.reKyc.Model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAddress {
    @NotNull
    private String loanNo;
    @NotNull
    private  String documentId;
    @NotNull
    private String documentType;
    @NotNull
    private String updatedAddress;
    @NotNull
    private String mobileNo;
    @NotNull
    private String otpCode;
}
