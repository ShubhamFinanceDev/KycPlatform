package com.example.reKyc.Repository;

import com.example.reKyc.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    @Query("select ad from Admin ad where ad.email=:email and ad.password=:password")
            Admin adminAccount(String email, String password) ;

}
