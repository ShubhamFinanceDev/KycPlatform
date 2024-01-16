package com.example.reKyc.Service;

import com.example.reKyc.Entity.OtpDetails;
import com.example.reKyc.Repository.OtpDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class OtpService implements UserDetailsService {
    /**
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Autowired
    private OtpDetailsRepository otpDetailsRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

       OtpDetails otpDetails= otpDetailsRepository.checkOtp(username).orElseThrow(() -> new RuntimeException("user not found"));
       return otpDetails;

    }
}
