package com.verify.panverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


import java.time.LocalDate;


public record PanVerificationRequest (

    @NotBlank
    @Pattern(
            regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}"
    )
    String panNumber,

    @NotBlank
     String fullName,

    @NotBlank
    String fathername,

    @NotNull
    LocalDate dob
){
}
