package com.project.shift.global.exception.detail.auth;

import org.springframework.http.HttpStatus;

import com.project.shift.global.exception.BusinessException;

public class AuthUnauthorizedException extends BusinessException {
	public AuthUnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, "Authentication Failed", message);
    }
}
