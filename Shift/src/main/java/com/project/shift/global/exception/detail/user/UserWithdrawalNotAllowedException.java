package com.project.shift.global.exception.detail.user;

import org.springframework.http.HttpStatus;

import com.project.shift.global.exception.BusinessException;

public class UserWithdrawalNotAllowedException extends BusinessException {
	public UserWithdrawalNotAllowedException(String message) {
        super(HttpStatus.BAD_REQUEST, "Withdrawal Rejected", message);
    }
}
