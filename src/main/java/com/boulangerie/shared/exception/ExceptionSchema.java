package com.boulangerie.shared.exception;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
public class ExceptionSchema {

    private Instant timestamp;
    private int status;
    private String message;
    private String path;
    private Map<String, String> errors;
    private String errorId;

    public ExceptionSchema(int status, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
        this.path = path;
    }

    public ExceptionSchema(int status, String message, String path, Map<String, String> errors) {
        this(status, message, path);
        this.errors = errors;
    }

    public ExceptionSchema(int status, String message, String path, String errorId) {
        this(status, message, path);
        this.errorId = errorId;
    }
}