package com.verify.panverification.dto;

public record PanRequest (

        String pan,
        String name,
        String fathername,
        String dob
){
}
