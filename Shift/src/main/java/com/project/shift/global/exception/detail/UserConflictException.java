package com.project.shift.global.exception.detail;

import org.springframework.http.HttpStatus;

import com.project.shift.global.exception.BusinessException;

public class UserConflictException extends BusinessException {
	public UserConflictException(String message) {
        super(HttpStatus.CONFLICT, "Conflict", message);
    }
}
