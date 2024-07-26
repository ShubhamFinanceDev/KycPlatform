package com.example.reKyc.Repository;

import com.example.reKyc.Entity.KycCustomer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KycCustomerRepository extends JpaRepository<KycCustomer,String> {
   @Query("select cd from KycCustomer cd where cd.loanNumber=:loanNo and cd.kycFlag='Y'")
   Optional<KycCustomer> getCustomer(String loanNo);
   @Transactional
   @Modifying
   @Query("update KycCustomer cd set cd.kycFlag='N' where cd.loanNumber=:loanNo")
    void updateKycFlag(String loanNo);
   @Query("SELECT e.mobileNo FROM KycCustomer e WHERE e.smsFlag='N'")
   List<String> findMobileNo();
   @Transactional
   @Modifying
   @Query("update KycCustomer k set k.smsFlag = 'Y' where k.mobileNo =:mobileList")
   void updateSmsSentFlag(String mobileList);
   @Query("SELECT e.mobileNo FROM KycCustomer e WHERE e.kycFlag='Y'")
   List<String> findMobileNumber();

}
