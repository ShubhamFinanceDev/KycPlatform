package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;

@Entity
@Data
@Table(name="Updated_Details")
public class UpdatedDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="update_id")
    private Long updatedId;
    @Column(name="loan_no")
    private String loanNo;
    @Column(name="document_id")
    private  String documentId;
    @Column(name="document_type")
    private String documentType;
    @Column(name="updated_address")
    private String updatedAddress;
    @Column(name="mobile_no")
    private String mobileNo;
    @Column(name="updated_date")
    private Date updatedDate;
}
