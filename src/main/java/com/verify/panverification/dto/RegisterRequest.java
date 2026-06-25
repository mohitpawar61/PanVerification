package com.verify.panverification.dto;

public record RegisterRequest (

     String FullName,

     String email,

     String password,

     String role
) {}

