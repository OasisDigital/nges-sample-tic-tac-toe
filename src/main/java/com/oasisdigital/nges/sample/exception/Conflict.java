package com.oasisdigital.nges.sample.exception;

public class Conflict extends RuntimeException {
    private static final long serialVersionUID = 2809400533276015955L;

    public Conflict() {
        super();
    }

    public Conflict(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Conflict(String message, Throwable cause) {
        super(message, cause);
    }

    public Conflict(String message) {
        super(message);
    }

    public Conflict(Throwable cause) {
        super(cause);
    }

}
