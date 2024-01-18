package com.example.reKyc.Repository;

import com.example.reKyc.Entity.OtpDetails;
import com.example.reKyc.Entity.UpdatedDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpdatedDetailsRepository extends JpaRepository<UpdatedDetails,Long> {

}
