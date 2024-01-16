package com.example.reKyc.Model;

import lombok.Data;
@Data
public class AadharOtpResponse {
        private Data data;
        private int statusCode;
        private String message;
    @lombok.Data
        public static class Data {
            private String requestId;
            private boolean otpSentStatus;
            private boolean ifNumber;
            private boolean isValidAadhaar;
            private String status;
        }
    }


