package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Entity
@Table(name = "customer_details")
@Data
public class CustomerDetails {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "application_number")
    private String applicationNumber;
    @Column(name = "loan_number")
    private String loanAccountNo;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "address_details_residential")
    private String residentialAddress;

    @Column(name = "identification_type")
    private String identificationType;

    @Column(name = "identification_number")
    private String identificationNumber;

    @Column(name = "phone_number")
    private String phoneNumber;


}
