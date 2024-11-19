package com.example.reKyc.Model;

import lombok.Data;
@Data
public class ResponseOfBase64 {

    private File file;
    @Data
    public static class File{

        public Long id;
        public String filetype;
        public String size;
        public String directURL;
        public String protected1;

    }
}
