package com.verify.panverification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pan_verification")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
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


    @Column(nullable = false, updatable = false)
    private LocalDateTime verifiedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @PrePersist
    public void prePersist() {
        verifiedAt = LocalDateTime.now();
    }
}
