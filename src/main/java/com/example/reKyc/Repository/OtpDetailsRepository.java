package com.example.reKyc.Repository;

import com.example.reKyc.Entity.OtpDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OtpDetailsRepository extends JpaRepository<OtpDetails,Long> {


    @Query("select c from OtpDetails c where c.mobileNo=:mobileNo")
    Optional<OtpDetails> checkOtp(String mobileNo);
    @Query("select otp from OtpDetails otp where otp.mobileNo=:mobileNo AND otp.otpCode=:otpCode And otp.otpId=:otpId")
    OtpDetails verifyOtpOfUser(String mobileNo, String otpCode, String otpId);
    @Query("select count(otp) from OtpDetails otp where otp.mobileNo=:mobileNumber")
    int countByMobile(String mobileNumber);
    @Modifying
    @Transactional
    @Query("Delete from OtpDetails otp where otp.mobileNo=:mobileNumber")
    void deletePreviousOtp(String mobileNumber);
}
