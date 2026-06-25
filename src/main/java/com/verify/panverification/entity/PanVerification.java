package com.verify.panverification.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pan_verification")
@Data
public class PanVerification {

    @Id
    @GeneratedValue(strategy =
    GenerationType.IDENTITY)
    private Long id;

    private String panNumber;

    private String fullName;

    private String fathername;

    private LocalDate dob;

    private String panStatus;

    private String verificationStatus;

    private LocalDateTime verifiedAt;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}
