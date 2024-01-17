package com.example.reKyc.Utill;


import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class MaskDocument {

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


    public String documentNoEncryption(String documentNo,String documentType)
    {
        String documentEncrypt;
        if(documentType.equals("aadharNo")) {
             documentEncrypt = "************".concat(documentNo.substring(documentNo.length() - 4, documentNo.length()));
        }
        else
        {
            documentEncrypt = "******".concat(documentNo.substring(documentNo.length() - 4, documentNo.length()));

        }

        return documentEncrypt;
    }
    public boolean compareAadharNoEquality(String maskId,String documentId){

        String maskid=maskId.substring(maskId.length()-4,maskId.length());
        String documentid=documentId.substring(documentId.length()-4,documentId.length());
        System.out.println("mask"+ maskId);
        System.out.println("not mask"+documentId);


        if (maskid.equals(documentid)) {
            return true;
        }
        else
        {
            return false;
        }
    }


}
