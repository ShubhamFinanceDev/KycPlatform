package com.example.reKyc.Repository;

import com.example.reKyc.Entity.CustomerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerDetailsRepository extends JpaRepository<CustomerDetails,Long> {
  @Query("select ld from CustomerDetails ld where ld.loanNumber=:loanNo")
    Optional<CustomerDetails> getLoanDetail(String loanNo);
  @Query("select ld from CustomerDetails ld where ld.loanNumber=:loanNo")
  CustomerDetails getLoanDetails(String loanNo);
  @Query("select count(ld) from CustomerDetails ld where ld.loanNumber=:loanNo")
  int getCount(String loanNo);
}
