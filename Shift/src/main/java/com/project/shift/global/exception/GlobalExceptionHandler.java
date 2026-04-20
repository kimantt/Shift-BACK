package com.project.shift.global.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException exception,
                                                 HttpServletRequest request) {
        return createProblemDetail(exception, request);
    }

    private ProblemDetail createProblemDetail(BusinessException exception,
                                              HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        problemDetail.setTitle(exception.getTitle());
        problemDetail.setProperty("path", request.getRequestURI());
        return problemDetail;
    }
}
