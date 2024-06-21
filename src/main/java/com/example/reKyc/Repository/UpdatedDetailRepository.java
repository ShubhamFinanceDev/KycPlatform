package com.example.reKyc.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdatedDetailRepository extends JpaRepository<com.example.reKyc.Entity.UpdatedDetails,Long> {
}
