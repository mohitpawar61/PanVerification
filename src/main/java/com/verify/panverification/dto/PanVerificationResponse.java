package com.verify.panverification.dto;

public record PanVerificationResponse(

        String panNumber,

        String panStatus,

        String verificationStatus,

        String message
) {
}
