package com.verify.panverification.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "protean_output_data")
@Data
public class ProteanOutputData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pan;
    private String panStatus;
    private String name;
    private String fathername;
    private String dob;
    private String seedingStatus;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private ProteanResponseHeader header;
}