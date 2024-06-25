package com.example.reKyc.Repository;

import com.example.reKyc.Entity.UpdatedDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdatedDetailRepository extends JpaRepository<UpdatedDetails,Long> {

}
