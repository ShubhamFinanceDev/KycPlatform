package com.example.reKyc.Model;

import lombok.Builder;
import lombok.Data;

@Data
//@Builder
public class JwtResponse extends CommonResponse {
    private String jwtToken;
    private String mobileNo;
    private String name;
    private String aadharNo;
    private String panNo;
    private String address;
    private String loanNo;
    private String voterIdNo;

    public JwtResponse() {
        this.jwtToken = "";
        this.mobileNo = "";
        this.name = "";
        this.aadharNo = "";
        this.panNo = "";
        this.address = "";
        this.loanNo = "";
    }
}
