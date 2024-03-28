package com.example.reKyc.Repository;

import com.example.reKyc.Entity.DdfsUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DdfsUploadRepository extends JpaRepository<DdfsUpload,Long> {
@Query("select count(distinct dd.loanNo) from DdfsUpload dd")
    Integer getUpdatedCount();
}
