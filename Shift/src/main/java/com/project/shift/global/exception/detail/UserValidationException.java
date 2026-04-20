package com.project.shift.global.exception.detail;

import org.springframework.http.HttpStatus;

import com.project.shift.global.exception.BusinessException;

public class UserValidationException extends BusinessException {
	public UserValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, "Validation Failed", message);
    }
}
