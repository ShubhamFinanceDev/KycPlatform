package com.example.reKyc.Service;

import com.example.reKyc.Entity.CustomerDetails;
import com.example.reKyc.Model.CustomerDataResponse;
import com.example.reKyc.Repository.CustomerDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanNoAuthentication implements UserDetailsService {

    @Autowired
    private CustomerDetailsRepository customerDetailsRepository;
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(UserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String loanNo) throws UsernameNotFoundException {

        return customerDetailsRepository.getLoanDetail(loanNo).orElseThrow(() -> new RuntimeException("user not found"));

    }
    @Async
    public void getCustomerData(String loanNo) {
        CustomerDataResponse customerDataResponse = new CustomerDataResponse();

        String sql = Query.loanQuery.concat("'" + loanNo + "'");
        try {
            List<com.example.reKyc.Model.CustomerDetails> customerDetails = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(com.example.reKyc.Model.CustomerDetails.class));
            logger.info("Data fetched successfully.");
            customerDataResponse.setCustomerName(customerDetails.get(0).getCustomer_Name());
            customerDataResponse.setLoanNumber(customerDetails.get(0).getLOAN_ACCOUNT_NO());
            customerDataResponse.setApplicationNumber(customerDetails.get(0).getApplication_Number());
//            customerDataResponse.setMobileNumber(customerDetails.get(0).getPHONE_NUMBER());
            customerDataResponse.setMobileNumber("8160041657");
            customerDataResponse.setAddressDetailsResidential(customerDetails.get(0).getRESIDENTIAL_ADDRESS());

            for (com.example.reKyc.Model.CustomerDetails data : customerDetails) {
                if (data.getIDENTIFICATION_TYPE().contains("PAN")) {
                    customerDataResponse.setPanNumber(data.getIDENTIFICATION_NUMBER());
                } else if (data.getIDENTIFICATION_TYPE().contains("AAdhar_No")) {
                    // customerDataResponse.setAadharNumber(data.getIDENTIFICATION_NUMBER());
                    customerDataResponse.setAadharNumber("390920211147");

                }
            }
            saveLoanNoDetailsLocally(customerDataResponse);
        } catch (Exception e) {
            logger.error("exception while running main db query :"+e.getMessage());
        }
    }

    private void saveLoanNoDetailsLocally(CustomerDataResponse customerDataResponse) {
        CustomerDetails customerDetails =new CustomerDetails();
        try
        {
            customerDetails.setLoanNumber(customerDataResponse.getLoanNumber());
            customerDetails.setAddressDetailsResidential(customerDataResponse.getAddressDetailsResidential());
            customerDetails.setAadhar(customerDataResponse.getAadharNumber());
            customerDetails.setApplicationNumber(customerDataResponse.getApplicationNumber());
            customerDetails.setCustomerName(customerDataResponse.getCustomerName());
            customerDetails.setMobileNumber(customerDataResponse.getMobileNumber());
            customerDetailsRepository.save(customerDetails);
            logger.info("Loan-detail save temporary.");

        }
        catch (Exception e) {
            logger.error("Error while saving temporary Loan detail.{}", e.getMessage());
        }
    }
}
