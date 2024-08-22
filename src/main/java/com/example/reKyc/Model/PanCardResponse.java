package com.example.reKyc.Model;

import lombok.Data;

import java.util.List;
@Data
public class PanCardResponse {
        private boolean getRelativeData;
        private List<String> files;
        private String type;
        private int id;
        private PanCardResult result;


@Data
 public static  class PanCardResult {
        private String dob;
        private String name;
        private String fatherName;
        private String number;
    private RelativeDetails relativeDetails;
    }
@Data
 public static  class PanCardSummary {
        private String number;
        private String name;
        private String dob;
        private String address;
        private SplitAddress splitAddress;
        private String gender;
        private String guardianName;
        private String issueDate;
        private String expiryDate;

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
public static class RelativeDetails {
        private String name;
        private String relation;

    }

}
