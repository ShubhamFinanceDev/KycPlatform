package com.example.reKyc.Repository;

import com.example.reKyc.Entity.GenerateDDFSKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerateDDFSKeyRepository extends JpaRepository<GenerateDDFSKey,Long> {

    @Query("select cd.genratedKey from GenerateDDFSKey cd")
    String getGeneratedKye();
}
