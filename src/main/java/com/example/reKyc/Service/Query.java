package com.example.reKyc.Service;

public class Query {
public static final String loanQuery="SELECT \n" +
        "    IC.\"Application Number\" AS Application_Number,\n" +
        "    IDE.\"LOAN_ACCOUNT_NO\",\n" +
        "    IC.\"Customer Name\" AS Customer_Name,\n" +
        "    AD.\"Address 1\" || ',' || AD.\"City\" || ',' || AD.\"State\" || ',' || AD.\"Pincode\" AS RESIDENTIAL_ADDRESS,\n" +
        "    IDE.\"PHONE_NUMBER\",\n" +
        "    IDE.\"IDENTIFICATION_NUMBER\",\n" +
        "    IDE.\"IDENTIFICATION_TYPE\"\n" +
        "FROM \n" +
        "    NEO_CAS_LMS_SIT1_SH.\"Individual Customer\" IC\n" +
        "LEFT JOIN NEO_CAS_LMS_SIT1_SH.\"Identification Details\" IDE\n" +
        "    ON IC.\"Neo CIF ID\" = IDE.\"CUSTOMER_INFO_FILE_NUMBER\"\n" +
        "LEFT JOIN NEO_CAS_LMS_SIT1_SH.\"Address Details\" AD\n" +
        "    ON IC.\"Customer Number\" = AD.\"Customer Number\"\n" +
        "WHERE \n" +
        "    IC.\"Applicant Type\" = 'Primary Applicant' \n" +
//        "    AND IDE.\"IDENTIFICATION_TYPE\" = 'AAdhar_No' \n" +
        "    AND AD.\"Addresstype\" = 'Residential Address' \n" +
        "    AND IDE.\"LOAN_ACCOUNT_NO\" =";


    // public static final String loanQuery="select * from NEW_CUSTOMER_DETAILS where LOAN_ACCOUNT_NO=";

    public static final String identificationQuery="select * from NEO_CAS_LMS_SIT1_SH.\"Identification Details\" where LOAN_ACCOUNT_NO=";
    public static final String addressIdentificationQuery="SELECT AP.\"Application Number\",\n" +
            "            IC.\"Customer Name\",\n" +
            "            AP.\"Loan Account No\",\n" +
            "            ID.PHONE_NUMBER as MOBILE_NUMBER,\n" +
            "            ID.IDENTIFICATION_TYPE,\n" +
            "            ID.IDENTIFICATION_NUMBER,\n" +
            "            AD.\"Address 1\"|| ',' || AD.\"City\" || ',' || AD.\"State\" || ',' || AD.\"Pincode\" AS RESIDENTIAL_ADDRESS,\n" +
            "            AD.\"Addresstype\"\n" +
            "            \n" +
            "    FROM neo_cas_lms_sit1_sh.application_newprod AP\n" +
            "    LEFT JOIN NEO_CAS_LMS_SIT1_SH.\"Individual Customer\" IC ON AP.\"Customer Number\"= IC.\"Customer Number\"\n" +
            "    LEFT JOIN NEO_CAS_LMS_SIT1_SH.\"Identification Details\" ID on ID.LOAN_ACCOUNT_NO=AP.\"Loan Account No\"\n" +
            "    LEFT JOIN NEO_CAS_LMS_SIT1_SH.\"Address Details\" AD on AD.\"Customer Number\"=IC.\"Customer Number\"\n" +
            "WHERE \n" +
            "    AD.\"Addresstype\"='Residential Address' AND\n" +
            "   AP.\"Loan Account No\" = ";
}
