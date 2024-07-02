package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.CustomerDataResponse;
import com.example.reKyc.Repository.CustomerDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
@EnableAsync
@Service
public class LoanNoAuthentication implements UserDetailsService {

    @Autowired
    private CustomerDetailsRepository customerDetailsRepository;
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(UserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String loanNo) throws UsernameNotFoundException {

        return customerDetailsRepository.getLoanDetail(loanNo).orElseThrow(() -> new RuntimeException("user not found"));

    }

}
