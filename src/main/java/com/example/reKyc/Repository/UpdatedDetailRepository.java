package com.example.reKyc.Repository;

import com.example.reKyc.Entity.UpdatedDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdatedDetailRepository extends JpaRepository<UpdatedDetails,String> {
    @Query("select count(distinct up.loanNumber) from UpdatedDetails up where up.rekycStatus='N'")
    Integer getKycCountDetail();
    @Query("select count(distinct up.loanNumber) from UpdatedDetails up where up.rekycStatus='Y'")
    Integer getUpdatedCount();
}
