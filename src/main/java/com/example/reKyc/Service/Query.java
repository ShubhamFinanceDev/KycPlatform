package com.example.reKyc.Service;

public class Query {
public static final String loanQuery="SELECT distinct IC.\"Application Number\" As Application_Number,\r\n" + //
        "        \r\n" + //
        "       IDE.\"LOAN_ACCOUNT_NO\",\r\n" + //
        "       IC.\"Customer Name\" As Customer_Name, \r\n" + //
        "      AD.\"Address 1\" As RESIDENTIAL_ADDRESS,\r\n" + //
        "       IDE.\"PHONE_NUMBER\",\r\n" + //
        "       IDE.IDENTIFICATION_NUMBER,\r\n" + //
        "       IDE.IDENTIFICATION_TYPE\r\n" + //
        "       FROM NEO_CAS_LMS_SIT1_SH.\"Individual Customer\" IC \r\n" + //
        "         INNER JOIN  NEO_CAS_LMS_SIT1_SH.\"Identification Details\" IDE ON  upper(IC.\"Neo CIF ID\") = upper(IDE.CUSTOMER_INFO_FILE_NUMBER)                           \r\n" + //
        "        INNER JOIN  NEO_CAS_LMS_SIT1_SH.\"Address Details\" AD ON IC.\"Customer Number\" = AD.\"Customer Number\"\r\n" + //
        "         WHERE IC.\"Applicant Type\" = 'Primary Applicant' and AD.\"Addresstype\" = 'Residential Address' and IDE.IDENTIFICATION_TYPE='AAdhar_No' and IDE.LOAN_ACCOUNT_NO=\r\n" + //
        "";


    // public static final String loanQuery="select * from NEW_CUSTOMER_DETAILS where LOAN_ACCOUNT_NO=";

}
