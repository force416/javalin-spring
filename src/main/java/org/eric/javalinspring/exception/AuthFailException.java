package org.eric.javalinspring.exception;

public class AuthFailException extends RuntimeException {
    public AuthFailException() {
        this("");
    }
    public AuthFailException(String message) {
        this(message, (Throwable) null);
    }

    public AuthFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
