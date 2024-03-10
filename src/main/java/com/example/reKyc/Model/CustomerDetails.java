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
public class CustomerDetails  {
//    private Long USER_ID;
    private String Application_Number;
    private String LOAN_ACCOUNT_NO;
    private String Customer_Name;
//    private String ADDRESS_DETAILS_RESIDENTIAL;
    private String PHONE_NUMBER;
    private String IDENTIFICATION_NUMBER;
    private String IDENTIFICATION_TYPE;



}
