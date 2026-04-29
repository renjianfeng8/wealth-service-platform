package com.finance.common.exception;

import lombok.Data;

@Data
public class ServiceException extends RuntimeException {
    private int code;

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message) {
        this(500, message);
    }
}