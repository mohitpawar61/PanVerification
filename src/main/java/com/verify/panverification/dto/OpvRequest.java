package com.verify.panverification.dto;

import java.util.List;

public record OpvRequest (

        String User_ID,
        String Records_count,
        String Request_time,
        String Transaction_ID,
        String Version,

        List<PanRequest> inputData,
        String signature

){
}
