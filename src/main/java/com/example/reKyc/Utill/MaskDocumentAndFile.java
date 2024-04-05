package com.example.reKyc.Utill;


import com.example.reKyc.Model.InputBase64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class MaskDocumentAndFile {



    public String documentNoEncryption(String documentNo) {

        String documentEncrypt = "";
        for (int i = 0; i < documentNo.length() - 4; i++) {
            documentEncrypt = documentEncrypt + "*";

        }
        String subString = documentNo.substring(documentNo.length() - 4, documentNo.length());
        documentEncrypt = documentEncrypt + subString;

        return documentEncrypt;
    }

    public boolean compareDocumentNumber(String extractedId, String documentId, String documentType) {

        String subExtractedId = extractedId.substring(extractedId.length() - 4, extractedId.length());

        boolean comparison = false;
        if (documentType.equals("aadhar")) {
            String aadharNo = documentId.substring(documentId.length() - 4, documentId.length());
            if (aadharNo.equals(subExtractedId)) {
                comparison = true;
            }
        } else {
            if (extractedId.equals(documentId)) {
                comparison = true;
            }
        }
        return comparison;

    }


}








