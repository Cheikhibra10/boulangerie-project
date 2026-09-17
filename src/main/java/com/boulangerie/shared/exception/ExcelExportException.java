package com.boulangerie.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ExcelExportException extends RuntimeException {

    public ExcelExportException(String message, Throwable cause) {
        super(message, cause);
    }
}