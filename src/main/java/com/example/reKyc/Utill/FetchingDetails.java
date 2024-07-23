package com.example.reKyc.Utill;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.CustomerDataResponse;
import com.example.reKyc.Repository.CustomerDetailsRepository;
import com.example.reKyc.Service.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FetchingDetails {
    @Autowired
    private CustomerDetailsRepository customerDetailsRepository;
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MaskDocumentNo maskDocumentNo;

    private final Logger logger = LoggerFactory.getLogger(UserDetailsService.class);


    @Async
    public CompletableFuture<List<CustomerDetails>> getCustomerIdentification(String loanNo) throws Exception {
        String jdbcQuery = Query.identificationQuery.concat("'" + loanNo + "'");
        List<CustomerDetails> customerDetailsList = jdbcTemplate.query(jdbcQuery, new BeanPropertyRowMapper<>(CustomerDetails.class));
        System.out.println(customerDetailsList.get(0));
        return CompletableFuture.completedFuture(customerDetailsList);

    }
    @Async
    public CompletableFuture<CustomerDataResponse> getCustomerData(String loanNo) throws Exception {

        CustomerDataResponse customerDataResponse=new CustomerDataResponse();

        List<CustomerDetails> customerIdentificationDetails = new ArrayList<>();
        if (loanNo.contains("_")) {
            customerIdentificationDetails= getCustomerIdentification(loanNo).get();
            List<CustomerDetails> customerDetails = customerDetailsRepository.getLoanDetails(loanNo);
            if (!customerDetails.isEmpty() && !customerIdentificationDetails.isEmpty()) {
                customerDataResponse.setCustomerName(customerDetails.get(0).getCustomerName());
                customerDataResponse.setApplicationNumber(customerDetails.get(0).getApplicationNumber());
                customerDataResponse.setAddressDetailsResidential(customerDetails.get(0).getResidentialAddress());
                customerDataResponse.setPhoneNumber(customerIdentificationDetails.get(0).getPhoneNumber());
                customerDataResponse.setCustomerName(customerDetails.get(0).getCustomerName());
                customerDataResponse.setLoanNumber(customerDetails.get(0).getLoanAccountNo());
                for (CustomerDetails customerDetails1 : customerIdentificationDetails) {
                    if(customerDetails1.getIdentificationType().contains("PAN"))
                    {
                        customerDataResponse.setPanNumber(customerDetails1.getIdentificationNumber());
                    } else if (customerDetails1.getIdentificationType().contains("AAdhar_No")) {
                        customerDataResponse.setAadharNumber(customerDetails1.getIdentificationNumber());
//                        customerDataResponse.setAadharNumber("390920211147");
                    }

                }
            }
        } else {
            String jdbcQuery = Query.loanQuery.concat("'" + loanNo + "'");
            customerIdentificationDetails = jdbcTemplate.query(jdbcQuery, new BeanPropertyRowMapper<>(CustomerDetails.class));
            logger.info("Data fetched successfully.");
            System.out.println(customerIdentificationDetails.get(0));
            customerDataResponse.setCustomerName(customerIdentificationDetails.get(0).getCustomerName());
            customerDataResponse.setLoanNumber(customerIdentificationDetails.get(0).getLoanAccountNo());
            customerDataResponse.setApplicationNumber(customerIdentificationDetails.get(0).getApplicationNumber());
            customerDataResponse.setPhoneNumber(customerIdentificationDetails.get(0).getPhoneNumber());
//            customerDataResponse.setMobileNumber("8160041657");
            customerDataResponse.setAddressDetailsResidential(customerIdentificationDetails.get(0).getResidentialAddress());

            for (CustomerDetails customerDetails1 : customerIdentificationDetails) {

                if (customerDetails1.getIdentificationType().contains("PAN")) {
                    customerDataResponse.setPanNumber(customerDetails1.getIdentificationNumber());
                } else if (customerDetails1.getIdentificationType().contains("AAdhar_No")) {
                    customerDataResponse.setAadharNumber(customerDetails1.getIdentificationNumber());
//                            customerDataResponse.setAadharNumber("390920211147");
                }

            }
        }
        customerDataResponse.setPanNumber(customerDataResponse.getPanNumber() != null ? maskDocumentNo.documentNoEncryption(customerDataResponse.getPanNumber()) : "NA");
        customerDataResponse.setAadharNumber(customerDataResponse.getAadharNumber() != null ? maskDocumentNo.documentNoEncryption(customerDataResponse.getAadharNumber()) : "NA");
        return CompletableFuture.completedFuture(customerDataResponse);
    }
}
