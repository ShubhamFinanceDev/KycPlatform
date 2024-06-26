package com.example.reKyc.Controller;

import com.example.reKyc.Entity.KycCustomer;
import com.example.reKyc.Model.AdminResponse;
import com.example.reKyc.Model.CommonResponse;
import com.example.reKyc.Model.KycCountUpload;
import com.example.reKyc.Repository.AdminRepository;
import com.example.reKyc.Repository.CustomerRepository;
import com.example.reKyc.Service.Service;
import com.example.reKyc.Utill.OtpUtility;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class Admin {

    @Autowired
    private Service service;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private OtpUtility otpUtility;

    @CrossOrigin
    @PostMapping("/invoke-kyc-process-flag")
    public ResponseEntity<?> invokeProcessFlag(@RequestParam("file") MultipartFile file, @RequestParam("uid") Long uid) {

        HashMap<String, String> response = new HashMap<>();
        List<String> contactList = new ArrayList<>();
        String errorMsg = "";
        try {
            if (adminRepository.findById(uid).isEmpty()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            List<KycCustomer> customerList = new ArrayList<>();
            InputStream inputStream = file.getInputStream();
            ZipSecureFile.setMinInflateRatio(0);                //for zip bomb detected
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.next();

            if (headerRow.getCell(0).toString().equals("Loan-No") && headerRow.getCell(1).toString().equals("Contact-No")) {

                while (rowIterator.hasNext()) {
                    KycCustomer customer = new KycCustomer();

                    Row row = rowIterator.next();
                    Cell cell = row.getCell(0);
                    Cell cell1 = row.getCell(1);
                    errorMsg = (cell == null || cell.getCellType() == CellType.BLANK) ? "File upload error due to row no " + (row.getRowNum() + 1) + " is empty" : "";

                    if (errorMsg.isEmpty()) {

                        customer.setLoanNumber(cell.toString());
                        contactList.add(cell1.toString());
                        customer.setKycFlag("Y");
                        customerList.add(customer);
                    } else {
                        response.put("msg", errorMsg);
                        response.put("code", "1111");
                        break;
                    }

                }
                if (errorMsg.isEmpty()) {
                    try {
                        customerRepository.saveAll(customerList);
                        service.sendOtpOnContactLists(contactList);
                        response.put("msg", "Successfully uploaded");
                        response.put("code", "0000");
                    } catch (Exception e) {
                        response.put("msg", "Technical error");
                        response.put("code", "1111");
                    }
                }
            } else {
                response.put("msg", "File format error");
                response.put("code", "1111");
            }

        } catch (Exception e) {
            response.put("msg", "Technical issue");
            response.put("code", "1111");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody HashMap<String, String> input) {
        CommonResponse commonResponse = new CommonResponse();
        AdminResponse adminResponse = new AdminResponse();
        String email = input.get("email");
        String password = input.get("password");
        Optional<com.example.reKyc.Entity.Admin> admin = adminRepository.adminAccount(email, password);   //check for email and password

        if (admin.isPresent()) {
            adminResponse.setMsg("Login successfully");
            adminResponse.setCode("0000");
            adminResponse.setUid(admin.get().getUid());
            return new ResponseEntity<>(adminResponse, HttpStatus.OK);
        }

        commonResponse.setMsg("Username password did not matched.");
        commonResponse.setCode("1111");
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }

    @GetMapping("/kycCount")
    public ResponseEntity<?> kycCount(@RequestParam("uid") Long uid) {

        if (adminRepository.findById(uid).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        KycCountUpload count = service.kycCount();   //to fetch and update KYC count
        return ResponseEntity.ok(count);
    }

}


