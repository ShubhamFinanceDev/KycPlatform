package com.example.reKyc.Repository;

import com.example.reKyc.Entity.CustomerDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerDetailsRepository extends JpaRepository<CustomerDetails,Long> {

    @Query("select cd from CustomerDetails cd where cd.loanNumber=:loanNumber AND cd.kycFlag='Y'")
    CustomerDetails findByLoanNumber(@Param("loanNumber")String loanNumber);
    @Query("select cd from CustomerDetails cd where cd.mobileNumber=:mobileNo")
    CustomerDetails findUserDetailByMobile(String mobileNo);
    @Query("select count(cd) from CustomerDetails cd where cd.loanNumber=:loanNumber  and cd.aadhar=:aadharNo")
    int checkAadharNo(String loanNumber,String aadharNo);

    @Query("select cd from CustomerDetails cd where cd.loanNumber=:loanNumber  and cd.aadhar=:aadharNo")
    CustomerDetails checkCustomerAadharNo(String loanNumber,String aadharNo);
    @Transactional
    @Modifying
    @Query("update CustomerDetails cd set cd.kycFlag='Y' where cd.loanNumber in :loanNo")
    Integer enableKycFlag(List<String> loanNo);
    @Query("select cd from CustomerDetails cd where cd.loanNumber=:loanNo  and cd.pan=:panNO")
    CustomerDetails checkCustomerPanNo(String loanNo, String panNO);
}
