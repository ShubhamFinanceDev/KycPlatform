package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Entity
@Data
@Table(name="kyc_customer")
public class KycCustomer {
    @Id
    @Column(name = "loan_number")
    private String loanNumber;
    @Column(name="kyc_flag")
    private String kycFlag;

}
