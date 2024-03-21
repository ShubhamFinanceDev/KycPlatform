package com.example.reKyc.Repository;

import com.example.reKyc.Entity.LoanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanDetailsRepository extends JpaRepository<LoanDetails,Long> {
  @Query("select ld from LoanDetails ld where ld.loanNumber=:loanNo")
    LoanDetails getLoanDetail(String loanNo);

  @Query("select count(ld) from LoanDetails ld where ld.loanNumber=:loanNo")
  int getCount(String loanNo);
}
