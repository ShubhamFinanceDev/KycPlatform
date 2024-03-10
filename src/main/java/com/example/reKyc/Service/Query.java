package com.example.reKyc.Service;

public class Query {
 public static final String loanQuery="    IC.\"Application Number\" AS Application_Number,\n\",\r\n" + //
            "       --IDE.\"LOAN_ACCOUNT_NO\",\r\n" + //
            "       IDE.\"LOAN_ACCOUNT_NO\",\n"+//
            "       IC.\"Customer Name\" As Customer_Name, \r\n" + //
            "--       AD.\"Address 1\",\r\n" + //
            "--       AD.\"Address 2\",\r\n" + //
            "--       AD.\"Address 3\",\r\n" + //
            "--       AD.\"Address 4\",\r\n" + //
            "       IDE.\"PHONE_NUMBER\",\r\n" + //
            "       IDE.IDENTIFICATION_NUMBER,\r\n" + //
            "       IDE.IDENTIFICATION_TYPE\r\n" + //
            "       FROM NEO_CAS_LMS_SIT1_SH.\"Individual Customer\" IC \r\n" + //
            "         INNER JOIN  NEO_CAS_LMS_SIT1_SH.\"Identification Details\" IDE ON  upper(IC.\"Neo CIF ID\") = upper(IDE.CUSTOMER_INFO_FILE_NUMBER)                           \r\n" + //
            "--         INNER JOIN  NEO_CAS_LMS_SIT1_SH.\"Address Details\" AD ON IC.\"Customer Number\" = AD.\"Customer Number\"\r\n" + //
            "         WHERE IC.\"Applicant Type\" = 'Primary Applicant' and IDE.LOAN_ACCOUNT_NO=";
}
