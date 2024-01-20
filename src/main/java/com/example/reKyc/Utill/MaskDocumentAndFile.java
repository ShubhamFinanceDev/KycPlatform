package com.example.reKyc.Utill;


import com.example.reKyc.Model.InputBase64;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class MaskDocumentAndFile {

//    @Value("${jwt.secret}")
//    private String jwtSecret;
//
//    @Value("${jwt.expiration}")
//    private long jwtExpirationMs;
//
//
//    public String generateToken(String username) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
//
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(SignatureAlgorithm.HS512, jwtSecret)
//                .compact();
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }


    public String documentNoEncryption(String documentNo, String documentType) {
        String documentEncrypt;
        String documentId = documentNo.substring(documentNo.length() - 4, documentNo.length());
        if (documentType.equals("aadharNo")) {
            documentEncrypt = "************".concat(documentId);
        } else {
            documentEncrypt = "******".concat(documentId);

        }

        return documentEncrypt;
    }

    public boolean compareAadharNoEquality(String maskId, String documentId) {

        String maskid = maskId.substring(maskId.length() - 4, maskId.length());
        String documentid = documentId.substring(documentId.length() - 4, documentId.length());
        System.out.println("mask" + maskId);
        System.out.println("not mask" + documentId);

        if (maskid.equals(documentid)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean generateFileLocally(InputBase64 inputParam) throws IOException {
        boolean status=true;

        String outputPath = "src/main/resources/DocumentImage/";
        int index = 0;
        for (InputBase64.Base64Data file : inputParam.getBase64Data()) {

            String base64Data = file.getBase64String();
            String fileType = (file.getFileType().contains("pdf")) ? "pdf":"jpg";
            String fileName="";
            if (index == 0) {
            fileName=outputPath+"front-"+inputParam.getLoanNo()+"." +fileType;
            } else {
                fileName= outputPath+"back-"+inputParam.getLoanNo()+ "." + fileType;
            }

            byte[] binaryData = Base64.getDecoder().decode(base64Data);

            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                fileOutputStream.write(binaryData);
                System.out.println("File saved successfully to: " + fileName);
                index++;
                status= true;
            } catch (IOException e) {
                e.printStackTrace();
                status= false;
                break;
            }

        }
        return status;
    }

public boolean createFileInDffs(String loanNo)
{
   String filepath="src/main/resources/DocumentImage";

    File folder = new File(filepath);
    File[] listOfFiles = folder.listFiles();
    boolean fileCreated=false;

    for (File file : listOfFiles) {
        if (file.getName().contains(loanNo)) {

            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get((filepath+"/"+file.getName())));
                String base64String = Base64.getEncoder().encodeToString(fileBytes);
                System.out.println("Base64 representation of the file:\n" + base64String);
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately
                fileCreated=false;
            }
        }
            System.out.println(file.getName());
            fileCreated= true;
        }
          return  fileCreated;
    }
}








