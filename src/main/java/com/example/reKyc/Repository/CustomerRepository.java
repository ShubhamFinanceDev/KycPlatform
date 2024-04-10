package com.example.reKyc.Repository;

import com.example.reKyc.Entity.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
   @Query("select cd from Customer cd where cd.loanNumber=:loanNo and cd.kycFlag='Y'")
   Customer getCustomer(String loanNo);
   @Transactional
   @Modifying
   @Query("update Customer cd set cd.kycFlag='N' where cd.loanNumber=:loanNo")
    void updateKycFlag(String loanNo);

   @Query("select count(cd) from Customer cd where cd.kycFlag='N'")
   Integer getKycCountDetail();
}
