package com.project.shift.global.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException exception,
                                                 HttpServletRequest request) {
		return createProblemDetail(exception.getStatus(), exception.getTitle(), exception.getMessage(), request);
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException exception,
                                                               HttpServletRequest request) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        String detail = errors.isEmpty() ? "입력값이 올바르지 않습니다." : String.join(", ", errors);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "요청 데이터 검증 실패",
                detail,
                request
        );
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }
	
	@ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception,
                                                   HttpServletRequest request) {
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected Server Error",
                "예기치 못한 오류가 발생했습니다.",
                request
        );
    }
	
	private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

	private ProblemDetail createProblemDetail(HttpStatus status,
								              String title,
								              String detail,
                                              HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("path", request.getRequestURI());
        return problemDetail;
    }
}
