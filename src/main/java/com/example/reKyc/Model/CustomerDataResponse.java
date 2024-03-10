package com.example.reKyc.Model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.util.Collection;

@Data
public class CustomerDataResponse  {

    private String applicationNumber;
    private String loanNumber;
    private String customerName;
    private String addressDetailsResidential;
    private String panNumber;
    private String aadharNumber;
    private String mobileNumber;

}
