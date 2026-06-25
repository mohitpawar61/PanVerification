package com.verify.panverification.dto;

import org.apache.poi.ss.formula.functions.T;

public record ApiResponse<T> (
        boolean success,
        String message,
        T data

){
}
