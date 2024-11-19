package com.example.reKyc.Model;

import lombok.Data;

import java.util.List;
@Data
public class AadharResponse {
    private Result result;
    private List<String> files;
    private int id;
    @Data
    public static class Result {
        private String uid;
        private String vid;
        private String name;
        private String dob;
        private String yob;
        private String pincode;
        private String address;
        private String gender;
        private SplitAddress splitAddress;
        private String uidHash;
        private Summary summary;
        private boolean validBackAndFront;
        private String dateOfBirth;
    }
    @Data
    public static class SplitAddress {
        private List<String> district;
        private List<List<String>> state;
        private List<String> city;
        private String pincode;
        private List<String> country;
        private String addressLine;
    }
    @Data
    public static class Summary {
        private String number;
        private String name;
        private String dob;
        private String address;
        private SplitAddress splitAddress;
        private String gender;
        private String guardianName;
        private String issueDate;
        private String expiryDate;
        private String dateOfBirth;
    }
}
