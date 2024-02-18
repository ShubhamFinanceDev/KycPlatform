package com.example.reKyc.Utill;


import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.InputBase64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;

@Component
public class MaskDocumentAndFile {

    //    @Value("${jwt.secret}")
//    private String jwtSecret;
//
    @Value("${file_path}")
    private String file_path;
    @Autowired
    private DdfsUtility ddfsUtility;

    public String documentNoEncryption(String documentNo) {

        String documentEncrypt = "";
        for (int i = 0; i < documentNo.length() - 4; i++) {
            documentEncrypt = documentEncrypt + "*";

        }
        String subString = documentNo.substring(documentNo.length() - 4, documentNo.length());
        documentEncrypt = documentEncrypt + subString;

        return documentEncrypt;
    }

    public boolean compareDocumentNumber(String extractedId, String documentId, String documentType) {

        String subExtractedId = extractedId.substring(extractedId.length() - 4, extractedId.length());
        boolean comparison = false;
        if (documentType.equals("aadhar")) {
            String aadharNo = documentId.substring(documentId.length() - 4, documentId.length());
            if (aadharNo.equals(subExtractedId)) {
                comparison = true;
            }
        } else {
            if (extractedId.equals(documentId)) {
                comparison = true;
            }
        }
        return comparison;

    }


    public boolean generateFileLocally(InputBase64 inputParam) throws IOException {
        boolean status = true;
        int index = 0;
        for (InputBase64.Base64Data file : inputParam.getBase64Data()) {

            String base64Data = file.getBase64String();
            String fileType = (file.getFileType().contains("pdf")) ? "pdf" : "jpg";
            String fileName = "";
            if (index == 0) {
                fileName = file_path + "front-" + inputParam.getLoanNo() + "." + fileType;
            } else {
                fileName = file_path + "back-" + inputParam.getLoanNo() + "." + fileType;
            }

            byte[] binaryData = Base64.getDecoder().decode(base64Data);

            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                fileOutputStream.write(binaryData);
                System.out.println("File saved successfully to: " + fileName);
                index++;
                status = true;
            } catch (IOException e) {
                e.printStackTrace();
                status = false;
                break;
            }

        }
        return status;
    }

////    public String createFileInDffs(String loanNo) {
////        File folder = new File(file_path);
////        File[] listOfFiles = folder.listFiles();
////        boolean fileCreated = false;
////        String base64String = null;
////        HashMap ddfsResponse=new HashMap<>();
////
////        for (File file : listOfFiles) {
////            if (file.getName().contains(loanNo)) {
////
////                try {
////                    System.out.println("===ddfs token==" + ddfsUtility.generateDDFSKey());
////                    byte[] fileBytes = Files.readAllBytes(Paths.get((file_path + file.getName())));
////                    base64String = Base64.getEncoder().encodeToString(fileBytes);
//////                System.out.println("Base64 representation of the file:\n" + base64String);
//////                    fileCreated = true;
////                    ddfsResponse= ddfsUtility.callDDFSApi(base64String);
////
////                } catch (Exception e) {
////                    e.printStackTrace(); // Handle the exception appropriately
////                }
////            }
////            System.out.println(file.getName());
////        }
////        return base64String;
//    }
}








