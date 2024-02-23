package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="Updated_Details")
public class DdfsUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="update_id")
    private Long updatedId;
    @Column(name="loan_no")
    private String loanNo;
    @Column(name="document_type")
    private String documentType;
    @Column(name="file_name")
    private String fileName;
    @Column(name="mobile_no")
    private String mobileNo;
    @Column(name="updated_date")
    private LocalDateTime updatedDate;
    @Column(name="ddfs_flag")
    private String ddfsFlag;
    @PrePersist
    public void prePersist() {
        this.updatedDate = LocalDateTime.now();
    }
}
