package com.example.reKyc.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="ddfs_key")
public class GenerateDDFSKey {

    @Id
    @Column(name="key_id")
    private Long keyId;
    @Column(name="genrated_key")
    private String genratedKey;
}
