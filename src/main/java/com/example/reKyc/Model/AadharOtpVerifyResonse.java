package com.example.reKyc.Model;

import lombok.Data;
@Data
public class AadharOtpVerifyResonse {

        private Data data;
        private int statusCode;
        private String message;

@lombok.Data
        public static class Data {
            private String clientId;

            private String full_name;

            private String aadhaar_number;

            private String dob;

            private String gender;

            private Address address;

            private boolean faceStatus;

            private int faceScore;

            private String zip;

            private String profileImage;

            private boolean hasImage;

            private String emailHash;

            private String mobileHash;

            private String rawXml;

            private String zipData;

            private String careOf;

            private String shareCode;

            private boolean mobileVerified;

            private String referenceId;

            private Object aadhaarPdf;

            private String status;

            private String uniquenessId;


        }
@lombok.Data
        public static class Address {
            private String country;

            private String dist;

            private String state;

            private String po;

            private String loc;

            private String vtc;

            private String subdist;

            private String street;

            private String house;

            private String landmark;

        }
    }


