package com.verify.panverification.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "protean_response_header")
@Data
public class ProteanResponseHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String recordsCount;
    private String responseTime;
    private String transactionId;
    private String version;
    private String responseCode;

    @OneToOne
    @JoinColumn(name = "pan_verification_id")
    private PanVerification panVerification;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL)
    private List<ProteanOutputData> outputData;
}