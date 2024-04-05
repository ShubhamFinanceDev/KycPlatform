package com.example.reKyc.Model;

import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InputBase64 {
    @NotNull
    private String loanNo;
    @NotNull
    private String documentId;
    @NotNull
    private String documentType;
    @NotNull
    private List<Base64Data> base64Data;

    @Data
    public static class Base64Data {
        @NotNull
        private String base64String;
        @NotNull
        private String fileType;
    }
}

