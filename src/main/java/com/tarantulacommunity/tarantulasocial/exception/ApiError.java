package com.tarantulacommunity.tarantulasocial.exception;

import java.util.Map;

public record ApiError(
        String message,
        String code,
        Map<String, String> fieldErrors
) {

    public ApiError(String message, String code) {
        this(message, code, null);
    }
}