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
    private Long USER_ID;
    private String APPLICATION_NUMBER;
    private String LOAN_NUMBER;
    private String CUSTOMER_NAME;
    private String ADDRESS_DETAILS_RESIDENTIAL;
    private String MOBILE_NUMBER;

    private String IDENTIFICATION_TYPE;

    private String IDENTIFICATION_NUMBER;


}
