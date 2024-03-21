package com.example.reKyc.Service;

public class Query {
public static final String loanQuery="SELECT IC.Application_Number AS Application_Number,\n" +
        "    IDE.LOAN_ACCOUNT_NO,\n" +
        "    IC.Customer_Name AS Customer_Name,\n" +
        "    AD.Address_1 || ',' || AD.City || ',' || AD.State || ',' || AD.Pincode AS Address,\n" +
        "    IDE.PHONE_NUMBER,\n" +
        "    IDE.IDENTIFICATION_NUMBER,\n" +
        "    IDE.IDENTIFICATION_TYPE\n" +
        "FROM \n" +
        "    Individual_Customer IC\n" +
        "LEFT JOIN Identification_Details IDE\n" +
        "    ON IC.NEO_CIF_ID = IDE.CUSTOMER_INFO_FILE_NUMBER\n" +
        "LEFT JOIN Address_Details AD\n" +
        "    ON IC.Customer_Number = AD.Customer_Number\n" +
        "WHERE \n" +
        "   IC.Applicant_Type = 'Primary Applicant'\n" +
        "   AND IDE.IDENTIFICATION_TYPE = 'AAdhar_No' \n" +
        "   AND AD.Addresstype = 'Residential Address' \n" +
        "   AND IDE.LOAN_ACCOUNT_NO =";


    // public static final String loanQuery="select * from NEW_CUSTOMER_DETAILS where LOAN_ACCOUNT_NO=";

}
