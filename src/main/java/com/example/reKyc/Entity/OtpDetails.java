package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Entity
@Table(name = "otp_detail")
@Data
public class OtpDetails implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="otp_id")
    private Long otpId;
    @Column(name="otp_code")
    private Long otpCode;
    @Column(name="mobile_number")
    private String mobileNo;
    @Column(name="expr_time")
    private String otpExprTime;
    @Column(name = "otp_password")
    private String otpPassword;



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
        return otpPassword;
    }

    /**
     * @return
     */
    @Override
    public String getUsername() {
        return mobileNo;
    }

    /**
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
