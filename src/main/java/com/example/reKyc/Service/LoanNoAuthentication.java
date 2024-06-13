package com.example.reKyc.Service;

import com.example.reKyc.Controller.User;
import com.example.reKyc.Entity.LoanDetails;
import com.example.reKyc.Model.CustomerDataResponse;
import com.example.reKyc.Model.CustomerDetails;
import com.example.reKyc.Repository.CustomerRepository;
import com.example.reKyc.Repository.LoanDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanNoAuthentication implements UserDetailsService {

    @Autowired
    private LoanDetailsRepository loanDetailsRepository;
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(UserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String loanNo) throws UsernameNotFoundException {

        return loanDetailsRepository.getLoanDetail(loanNo).orElseThrow(() -> new RuntimeException("user not found"));

    }

    public String getCustomerData(String loanNo) {
        CustomerDataResponse customerDataResponse = new CustomerDataResponse();

        String sql = Query.loanQuery.concat("'" + loanNo + "'");
        try {
            List<CustomerDetails> customerDetails = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CustomerDetails.class));
            logger.info("Data fetched successfully.");
            customerDataResponse.setCustomerName(customerDetails.get(0).getCustomer_Name());
            customerDataResponse.setLoanNumber(customerDetails.get(0).getLOAN_ACCOUNT_NO());
            customerDataResponse.setApplicationNumber(customerDetails.get(0).getApplication_Number());
            customerDataResponse.setMobileNumber(customerDetails.get(0).getPHONE_NUMBER());
//            customerDataResponse.setMobileNumber("8160041657");
            customerDataResponse.setAddressDetailsResidential(customerDetails.get(0).getRESIDENTIAL_ADDRESS());

            for (CustomerDetails data : customerDetails) {
                if (data.getIDENTIFICATION_TYPE().contains("PAN")) {
                    customerDataResponse.setPanNumber(data.getIDENTIFICATION_NUMBER());
                } else if (data.getIDENTIFICATION_TYPE().contains("AAdhar_No")) {
                    customerDataResponse.setAadharNumber(data.getIDENTIFICATION_NUMBER());
//                    customerDataResponse.setAadharNumber("390920211147");

                }
            }
            saveLoanNoDetailsLocally(customerDataResponse);
        } catch (Exception e) {
            logger.error("exception while running main db query :"+e.getMessage());
        }
        return customerDataResponse.getMobileNumber();

    }

    private void saveLoanNoDetailsLocally(CustomerDataResponse customerDataResponse) {
        LoanDetails loanDetails=new LoanDetails();
        try
        {
            loanDetails.setLoanNumber(customerDataResponse.getLoanNumber());
            loanDetails.setAddressDetailsResidential(customerDataResponse.getAddressDetailsResidential());
            loanDetails.setAadhar(customerDataResponse.getAadharNumber());
            loanDetails.setApplicationNumber(customerDataResponse.getApplicationNumber());
            loanDetails.setCustomerName(customerDataResponse.getCustomerName());
            loanDetails.setMobileNumber(customerDataResponse.getMobileNumber());
            loanDetailsRepository.save(loanDetails);
            logger.info("Loan-detail save temporary.");

        }
        catch (Exception e) {
            logger.error("Error while saving temporary Loan detail.{}", e.getMessage());
        }
    }
}
