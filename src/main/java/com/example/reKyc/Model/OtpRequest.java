package com.example.reKyc.Model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


@Data
@Builder

public class OtpRequest {
    @NotNull
    private String mobileNo;
    @NotNull
    private String otpCode;
    @NotNull
    private String loanNo;
}
