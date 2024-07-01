package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;

@Entity
@Data
@Table(name = "updated_details")
public class UpdatedDetails {
    @Id
    @Column(name = "loan_number")
    private String loanNumber;

    @Column(name = "rekyc_date")
    private Date rekycDate;

    @Column(name = "address_details")
    private String addressDetails;

    @Column(name = "rekyc_document")
    private String rekycDocument;

    @Column(name = "application_number")
    private String applicationNumber;

    @Column(name = "rekyc_status")
    private String rekycStatus;
}
