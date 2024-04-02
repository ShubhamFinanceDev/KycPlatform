package com.example.reKyc.Model;

import lombok.Data;

@Data
public class KycCountUpload extends CommonResponse{

    private Integer existingKyc;
    private Integer updatedKyc;

}
