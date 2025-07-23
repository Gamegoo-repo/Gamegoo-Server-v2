package com.gamegoo.gamegoo_v2.core.exception.common;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class ExceptionAdvice {

    // 커스텀 에러
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<?>> globalException(GlobalException ex) {
        ApiResponse<?> errorResponse = ApiResponse.builder()
                .status(ex.getStatus())
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    // @Valid 검증 실패 시 발생하는 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        ApiResponse<Object> errorResponse = ApiResponse
                .builder()
                .status(BAD_REQUEST)
                .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
                .code("VALID_ERROR")
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }


    // HTTP 요청 body 값이 잘못된 경우 발생하는 에러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof MismatchedInputException mismatchedInputException) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.of(BAD_REQUEST,
                            mismatchedInputException.getPath().get(0).getFieldName() + " 필드의 값이 잘못되었습니다."));
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.of(BAD_REQUEST, "확인할 수 없는 형태의 데이터가 들어왔습니다"));
    }

    // 메소드 파라미터 검증 실패 시 발생하는 에러
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handlerConstraintViolationException(ConstraintViolationException e) {
        // ConstraintViolationException에서 메시지 추출
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(ApiResponse.of(BAD_REQUEST, errorMessage));
    }

    // 컨트롤러 @Validated 검증 실패 시 발생하는 에러
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<?>> handlerHandlerMethodValidationException(HandlerMethodValidationException e) {
        // 모든 제약 위반 메시지를 추출
        List<String> errorMessages = e.getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return error.getDefaultMessage();
                    } else {
                        return error.getDefaultMessage();
                    }
                })
                .collect(Collectors.toList());

        // 첫 번째 오류 메시지만 반환
        String responseMessage = String.join(", ", errorMessages);

        return ResponseEntity.badRequest().body(ApiResponse.of(BAD_REQUEST, responseMessage));
    }

    // 필수 query parameter 누락 시 발생하는 에러
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        // 누락된 파라미터에 대한 에러 메시지 생성
        String errorMessage = String.format("%s 파라미터가 누락되었습니다.", e.getParameterName());

        return ResponseEntity.badRequest().body(ApiResponse.of(BAD_REQUEST, errorMessage));
    }

    // query parameter 값의 타입이 잘못된 경우 발생하는 에러
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        // 파라미터 이름 및 기대한 타입 추출
        String parameterName = e.getName();
        String expectedType = e.getRequiredType().getSimpleName();

        String errorMessage = String.format("%s 파라미터의 값은 %s 타입이어야 합니다.", parameterName, expectedType);
        return ResponseEntity.badRequest().body(ApiResponse.of(BAD_REQUEST, errorMessage));
    }

    // 서버 내부 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception e) {
        ErrorCode errorCode = ErrorCode._INTERNAL_SERVER_ERROR;

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getStatus())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

}
