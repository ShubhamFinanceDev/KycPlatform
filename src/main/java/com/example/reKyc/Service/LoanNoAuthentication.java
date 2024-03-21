package com.example.reKyc.Service;

import com.example.reKyc.Entity.Customer;
import com.example.reKyc.Model.CustomerDataResponse;
import com.example.reKyc.Model.CustomerDetails;
import com.example.reKyc.Repository.CustomerRepository;
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
    private CustomerRepository customerRepository;
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String loanNo) throws UsernameNotFoundException {

        return customerRepository.findById(loanNo).orElseThrow(() -> new RuntimeException("user not found"));

    }

    public CustomerDataResponse getCustomerData(String loanNo) {
        CustomerDataResponse customerDataResponse = new CustomerDataResponse();

        String sql = Query.loanQuery.concat("'" + loanNo + "'");
        try {
            List<CustomerDetails> customerDetails = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CustomerDetails.class));

            customerDataResponse.setCustomerName(customerDetails.get(0).getCustomer_Name());
            customerDataResponse.setLoanNumber(customerDetails.get(0).getLOAN_ACCOUNT_NO());
            customerDataResponse.setApplicationNumber(customerDetails.get(0).getApplication_Number());
//            customerDataResponse.setMobileNumber(customerDetails.get(0).getPHONE_NUMBER());
            customerDataResponse.setMobileNumber("8160041657");
            customerDataResponse.setAddressDetailsResidential(customerDetails.get(0).getRESIDENTIAL_ADDRESS());

            for (CustomerDetails data : customerDetails) {
                if (data.getIDENTIFICATION_TYPE().contains("PAN")) {
                    customerDataResponse.setPanNumber(data.getIDENTIFICATION_NUMBER());
                } else if (data.getIDENTIFICATION_TYPE().contains("AAdhar_No")) {
                    // customerDataResponse.setAadharNumber(data.getIDENTIFICATION_NUMBER());
                    customerDataResponse.setAadharNumber("390920211147");

                }
            }
        } catch (Exception e) {
            System.out.println("exception while running main db query");
        }
        return customerDataResponse;

    }
}
