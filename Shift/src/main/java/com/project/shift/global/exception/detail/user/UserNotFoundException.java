package com.project.shift.global.exception.detail.user;

import org.springframework.http.HttpStatus;

import com.project.shift.global.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
	public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "User Not Found", message);
    }
}
