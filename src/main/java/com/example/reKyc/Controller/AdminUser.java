package com.example.reKyc.Controller;

import com.example.reKyc.Entity.Admin;
import com.example.reKyc.Entity.Customer;
import com.example.reKyc.Model.AdminResponse;
import com.example.reKyc.Model.CommonResponse;
import com.example.reKyc.Model.KycCountUpload;
import com.example.reKyc.Repository.AdminRepository;
import com.example.reKyc.Repository.CustomerRepository;
import com.example.reKyc.Service.Service;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin
@Controller
public class AdminUser {

    @Autowired
    private Service service;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/invoke-kyc-process-flag")
    public HashMap<String,String> invokeProcessFlag(@RequestParam("file") MultipartFile file) {

        HashMap<String,String> response=new HashMap<>();
        String errorMsg = "";
        try {

            List<Customer> customerList = new ArrayList<>();
            InputStream inputStream = file.getInputStream();
            ZipSecureFile.setMinInflateRatio(0);                //for zip bomb detected
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.next();

            if (headerRow.getCell(0).toString().equals("Loan-No")) {

                while (rowIterator.hasNext()) {
                    Customer customer = new Customer();

                    Row row = rowIterator.next();
                    Cell cell = row.getCell(0);
                    errorMsg = (cell == null || cell.getCellType() == CellType.BLANK) ? "File upload error due to row no " + (row.getRowNum() + 1) + " is empty" : "";

                    if (errorMsg.isEmpty()) {

                        customer.setLoanNumber(cell.toString());
                        customer.setKycFlag("Y");
                        customerList.add(customer);
                    } else {
                        response.put("msg",errorMsg);
                        response.put("code","1111");
                        break;
                    }

                }
                if (errorMsg.isEmpty())
                {
                    try {
                        customerRepository.saveAll(customerList);
                        response.put("msg", "Successfully uploaded");
                        response.put("code", "0000");
                    }
                    catch (Exception e)
                    {
                        response.put("msg", "Technical error");
                        response.put("code", "1111");
                    }
                }
            }
            else
            {
                response.put("msg", "File format error");
                response.put("code", "1111");
            }

        } catch (Exception e) {
            response.put("msg", "Technical issue");
            response.put("code", "1111");
        }
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse> adminLogin(@RequestBody HashMap<String,String> input)
    {
        CommonResponse commonResponse=new CommonResponse();
        AdminResponse adminResponse=new AdminResponse();

        try
        {
           String email=input.get("email");
           String password=input.get("password");
           Admin admin=adminRepository.adminAccount(email,password);

            if(admin==null)
           {
               commonResponse.setMsg("Credentials did not matched");
               commonResponse.setCode("1111");
               return new ResponseEntity<>(commonResponse, HttpStatus.OK);

           }
           else
           {
               adminResponse.setMsg("Login successfully");
               adminResponse.setCode("0000");
               adminResponse.setUid(admin.getUid());
               return new ResponseEntity(adminResponse, HttpStatus.OK);

           }
        }
        catch (Exception e)
        {
            commonResponse.setMsg("Technical error.");
            commonResponse.setCode("1111");
            return new ResponseEntity<>(commonResponse, HttpStatus.OK);

        }
    }

    @GetMapping("/kycCount")
    public ResponseEntity<KycCountUpload> kycCount(){

        KycCountUpload count = new KycCountUpload();
        CommonResponse commonResponse = new CommonResponse();

        try {
             count = service.kycCount();

            return new ResponseEntity<>( count,HttpStatus.OK);
        }
        catch (Exception e){
            commonResponse.setCode("1111");
            commonResponse.setMsg("Something went wrong. please try again");
        }
        return new ResponseEntity( commonResponse,HttpStatus.OK);
    }

}


