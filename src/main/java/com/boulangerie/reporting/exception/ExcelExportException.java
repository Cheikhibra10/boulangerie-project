package com.boulangerie.reporting.exception;

public class ExcelExportException
        extends RuntimeException {

    public ExcelExportException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}