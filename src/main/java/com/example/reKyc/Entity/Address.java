package com.example.reKyc.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Address
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "address")
    private String address;

}
