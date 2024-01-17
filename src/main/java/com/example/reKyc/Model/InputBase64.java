package com.example.reKyc.Model;

import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import lombok.Data;

import java.util.List;

@Data
public class InputBase64 {

        private String loanNo;
        private String documentId;
        private String documentType;
         private List<Base64Data> base64Data;
@Data
    public static class Base64Data {
    private String base64String;
    private String fileType;
    }
    }

