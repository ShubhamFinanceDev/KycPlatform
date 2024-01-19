package com.example.reKyc.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="Uploaded_Document")
public class UploadedDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="upload_id")
    private Long uploadId;
    @Column(name="loan_no")
    private String loanNo;
    @Column(name="document_id")
    private  String documentId;
    @Column(name="document_type")
    private String documentType;
    @Column(name="front")
    private String front;
    @Column(name="back")
    private String back;
    @Column(name="updated_date")
    private LocalDateTime updatedDate;
    @PrePersist
    public void prePersist() {
        this.updatedDate = LocalDateTime.now();
    }

}
