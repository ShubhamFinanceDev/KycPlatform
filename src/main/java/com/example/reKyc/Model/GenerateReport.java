package com.example.reKyc.Model;

import jakarta.persistence.Column;
import lombok.Data;

import java.sql.Date;
@Data
public class GenerateReport {

    private String loanNumber;

    private Date rekycDate;

    private String addressDetails;

    private String rekycDocument;

    private String applicationNumber;

    private String rekycStatus;
}
