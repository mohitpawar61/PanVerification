package com.verify.panverification.dto;

import java.util.List;

public record RequestBody(

        List<InputDataDto> inputDataDtos,
        String signature
) {
}
