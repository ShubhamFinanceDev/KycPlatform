package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Repository.CustomerDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoanNoAuthentication implements UserDetailsService {
    /**
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
//    @Autowired
//    private OtpDetailsRepository otpDetailsRepository;
    @Autowired
    private CustomerDetailsRepository customerDetailsRepository;
    @Override
    public UserDetails loadUserByUsername(String loanNo) throws UsernameNotFoundException {

//       OtpDetails otpDetails= otpDetailsRepository.checkOtp(username).orElseThrow(() -> new RuntimeException("user not found"));
        CustomerDetails customerDetails= customerDetailsRepository.checkLoanNo(loanNo).orElseThrow(() -> new RuntimeException("user not found"));

        return customerDetails;

    }
}
