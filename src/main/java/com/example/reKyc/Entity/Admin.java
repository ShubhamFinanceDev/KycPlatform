package com.example.reKyc.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="admin")
public class Admin {
    @Id
    @Column(name="uid")
    private long uid;
    @Column(name="email")
    private String email;
    @Column(name = "password")
    private String password;
}
