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
    private String updatedId;
    private String loanNo;
    private  String documentId;
    private String documentType;
    private String updatedAddress;
    private String mobileNo;
    private Date updatedDate;
}
