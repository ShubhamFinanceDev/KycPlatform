package com.example.reKyc.Utill;


import com.example.reKyc.Model.CustomerDataResponse;
import org.springframework.stereotype.Component;

@Component
public class MaskDocumentNo {


    public boolean compareDocumentNumber(CustomerDataResponse customerDataResponse,String documentId, String documentType) {

        boolean comparison = false;
        documentId=documentId.substring(documentId.length()-4);

        switch (documentType)
        {
            case "pan":

                comparison= customerDataResponse.getPanNumber().substring(customerDataResponse.getPanNumber().length()-4).equals(documentId);
                break;
            case "aadhar":
                comparison= customerDataResponse.getAadharNumber().substring(customerDataResponse.getPanNumber().length()-4).equals(documentId);
                break;
            case "voterId":
                comparison= customerDataResponse.getVoterIdNumber().substring(customerDataResponse.getPanNumber().length()-4).equals(documentId);
            break;
        }

        return comparison;
}
}








