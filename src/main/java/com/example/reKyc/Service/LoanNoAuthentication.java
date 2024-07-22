package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Entity.KycCustomer;
import com.example.reKyc.Repository.KycCustomerRepository;
import com.example.reKyc.Utill.FetchingDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
public class LoanNoAuthentication implements UserDetailsService {

    @Autowired
    private KycCustomerRepository kycCustomer;
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(UserDetailsService.class);
    @Autowired
    private FetchingDetails fetchingDetails;

    @Override
    public KycCustomer loadUserByUsername(String loanNo) {
        try {
            return kycCustomer.getCustomer(loanNo).orElseThrow(() -> new RuntimeException("user not found"));
        } catch (Exception e) {
            throw new RuntimeException("user not found");
        }
    }



}


