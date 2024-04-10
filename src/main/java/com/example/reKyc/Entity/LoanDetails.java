package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "customer_details")
@Data
public class LoanDetails {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "application_number")
    private String applicationNumber;
    @Id
    @Column(name = "loan_number")
    private String loanNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "address_details_residential")
    private String addressDetailsResidential;

    @Column(name = "pan_no")
    private String pan;

    @Column(name = "aadhar_no")
    private String aadhar;

    @Column(name = "mobile_number")
    private String mobileNumber;

}
