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

        Customer customerDetails= customerRepository.findById(loanNo).orElseThrow(() -> new RuntimeException("user not found"));
        return customerDetails;

    }

    public CustomerDataResponse getCustomerData(String loanNo)
    {
        CustomerDataResponse customerDataResponse = new CustomerDataResponse();

        String sql=Query.loanQuery.concat("'"+loanNo+"'");
        try {
            List<CustomerDetails> customerDetails = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CustomerDetails.class));

            customerDataResponse.setCustomerName(customerDetails.get(0).getCUSTOMER_NAME());
            customerDataResponse.setLoanNumber(customerDetails.get(0).getLOAN_NUMBER());
            customerDataResponse.setApplicationNumber(customerDetails.get(0).getAPPLICATION_NUMBER());
            customerDataResponse.setMobileNumber(customerDetails.get(0).getMOBILE_NUMBER());
            customerDataResponse.setAddressDetailsResidential(customerDetails.get(0).getADDRESS_DETAILS_RESIDENTIAL());

            for (CustomerDetails data : customerDetails) {
                if (data.getIDENTIFICATION_TYPE().contains("PAN")) {
                    customerDataResponse.setPanNumber(data.getIDENTIFICATION_NUMBER());
                } else if (data.getIDENTIFICATION_TYPE().contains("AADHAR_NO")) {
                    customerDataResponse.setAadharNumber(data.getIDENTIFICATION_NUMBER());
                }


            }

        }
        catch (Exception e)
        {
            System.out.println("exception while running main db query");
        }
        return customerDataResponse;

    }
}
