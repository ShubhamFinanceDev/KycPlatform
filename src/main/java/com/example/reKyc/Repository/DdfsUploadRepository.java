package com.example.reKyc.Repository;

import com.example.reKyc.Entity.DdfsUpload;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DdfsUploadRepository extends JpaRepository<DdfsUpload,Long> {
@Query("select count(distinct dd.loanNo) from DdfsUpload dd where dd.ddfsFlag='Y'")
    Integer getUpdatedCount();

@Query("select dd from DdfsUpload dd where dd.loanNo=:loanNo and dd.ddfsFlag='N'")
    List<DdfsUpload> getImageUrl(String loanNo);

    @Query("select dd from DdfsUpload dd where dd.ddfsFlag='N' and dd.fileName is not null")
    List<DdfsUpload> deletePreviousDetail();
}
