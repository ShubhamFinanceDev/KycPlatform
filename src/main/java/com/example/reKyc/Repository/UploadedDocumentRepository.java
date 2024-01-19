package com.example.reKyc.Repository;

import com.example.reKyc.Entity.UpdatedDetails;
import com.example.reKyc.Entity.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument,Long> {
}
