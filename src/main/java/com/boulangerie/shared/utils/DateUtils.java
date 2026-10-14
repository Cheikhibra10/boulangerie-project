package com.boulangerie.shared.utils;

import com.boulangerie.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateUtils {
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    public static LocalDate requireDate(LocalDate value, String message) {
        if (value == null) {
            throw new BadRequestException(message);
        }
        return value;
    }


}