package com.example.reKyc.Controller;

import com.example.reKyc.Entity.Customer;
import com.example.reKyc.Repository.CustomerRepository;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class Admin {

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/invoke-kyc-process-flag")
    public HashMap invokeProcessFlag(@RequestParam("file") MultipartFile file) {

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
                        System.out.println(e);
                        response.put("msg", "Technical error");
                        response.put("code", "1111");
                    }
                }
            }

        } catch (Exception e) {
            errorMsg = "failure:" + e;
        }
        return response;
    }

}


