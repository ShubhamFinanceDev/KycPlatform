package com.example.reKyc.Model;

import lombok.Data;

@Data
public class CustomerDataResponse  {

    private String applicationNumber;
    private String loanNumber;
    private String customerName;
    private String addressDetailsResidential;
    private String panNumber;
    private String aadharNumber;
    private String phoneNumber;

}
