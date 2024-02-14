package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.util.Collection;

@Entity
@Table(name = "customer_details")
@Data
public class CustomerDetails implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "application_number")
    private String applicationNumber;

    @Column(name = "loan_number")
    private String loanNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "address_details_residential")
    private String addressDetailsResidential;

    @Column(name = "PAN")
    private String pan;

    @Column(name = "Aadhar")
    private String aadhar;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name="kyc_flag")
    private String kycFlag;
    @Column(name = "last_update_date")
    private Date lastUpdateDate;

    /**
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public String getUsername() {
        return loanNumber;
    }

    /**
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isEnabled() {
        return false;
    }
}
