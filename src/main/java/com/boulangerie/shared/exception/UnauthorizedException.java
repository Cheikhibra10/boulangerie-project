// shared/exception/UnauthorizedException.java
package com.boulangerie.shared.exception;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message);
    }
}