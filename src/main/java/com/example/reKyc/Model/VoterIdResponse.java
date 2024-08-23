package com.example.reKyc.Model;

import lombok.Data;

import java.util.List;

@Data
public class VoterIdResponse
{
    private boolean getRelativeData;
    private List<String> files;
    private String type;
    private int id;
    private VoterIdResult result;

    @Data
    public static class VoterIdResult
    {

        private String name;
        private String fatherName;
        private String state;
        private String ageAsOn;
        private String dob;
        private String epicNumber;
        private String gender;
    }

}
