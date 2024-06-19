package com.example.reKyc.Repository;

import com.example.reKyc.Entity.KycCustomer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<KycCustomer,String> {
   @Query("select cd from KycCustomer cd where cd.loanNumber=:loanNo and cd.kycFlag='Y'")
   Optional getCustomer(String loanNo);
   @Transactional
   @Modifying
   @Query("update KycCustomer cd set cd.kycFlag='N' where cd.loanNumber=:loanNo")
    void updateKycFlag(String loanNo);

   @Query("select count(cd) from KycCustomer cd where cd.kycFlag='N'")
   Integer getKycCountDetail();
}
