package com.example.reKyc.Repository;

import com.example.reKyc.Entity.UpdatedDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpdatedDetailRepository extends JpaRepository<UpdatedDetails,String> {
    @Query("select count(distinct up.loanNumber) from UpdatedDetails up where up.rekycStatus='N'")
    Integer getKycCountDetail();
    @Query("select count(distinct up.loanNumber) from UpdatedDetails up where up.rekycStatus='Y'")
    Integer getUpdatedCount();
    @Transactional
    @Modifying
    @Query("update UpdatedDetails up set up.rekycStatus='Y' where up.loanNumber=:loanNumber and up.rekycStatus is null")
    void updateKycStatus(String loanNumber);
    @Query("select up from UpdatedDetails up where up.rekycStatus is not null")
    List<UpdatedDetails> latestKycDetail();
}
