package com.example.reKyc.Utill;


import com.example.reKyc.Model.CustomerDataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class MaskDocumentNo {

    private final Logger logger = LoggerFactory.getLogger(MaskDocumentNo.class);

    public boolean compareDocumentNumber(CustomerDataResponse customerDataResponse,String documentId, String documentType) {

        boolean comparison = false;
        documentId=documentId.substring(documentId.length()-4);

        switch (documentType)
        {
            case "pan":

                comparison= customerDataResponse.getPanNumber().substring(customerDataResponse.getPanNumber().length()-4).equals(documentId);
                break;
            case "aadhar":
                comparison= customerDataResponse.getAadharNumber().substring(customerDataResponse.getAadharNumber().length()-4).equals(documentId);
                break;
            case "voterId":
                comparison= customerDataResponse.getVoterIdNumber().substring(customerDataResponse.getVoterIdNumber().length()-4).equals(documentId);
            break;
        }

        return comparison;
}
}








