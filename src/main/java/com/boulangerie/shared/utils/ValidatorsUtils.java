package com.boulangerie.shared.utils;

import com.boulangerie.shared.exception.BadRequestException;

public final class ValidatorsUtils{
    private ValidatorsUtils(){}

    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }
}