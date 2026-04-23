package com.project.shift.global.exception.detail.auth;

import org.springframework.http.HttpStatus;

import com.project.shift.global.exception.BusinessException;

public class AuthValidationException extends BusinessException {
	public AuthValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, "Auth Validation Failed", message);
    }
}
