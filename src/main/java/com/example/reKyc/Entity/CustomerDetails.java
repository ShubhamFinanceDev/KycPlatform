package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "customer_details")
@Data
public class CustomerDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "application_number")
    private String applicationNumber;

    @Column(name = "loan_number")
    private String loanNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "address_details_residential")
    private String addressDetailsResidential;

    @Column(name = "PAN")
    private String pan;

    @Column(name = "Aadhar")
    private String aadhar;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name="kyc_flag")
    private String kycFlag;
}
