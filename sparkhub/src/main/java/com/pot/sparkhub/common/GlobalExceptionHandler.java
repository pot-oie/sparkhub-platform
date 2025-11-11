package com.pot.sparkhub.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

// 捕获所有 @RestController 的异常
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获 AccessDeniedException (权限不足)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 403
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        return Result.error(403, "权限不足: " + e.getMessage());
    }

    // 捕获其他所有RuntimeException
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public Result<?> handleRuntimeException(RuntimeException e) {
        // 实际开发中, 你可能需要记录日志
        e.printStackTrace();
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }

    // 捕获其他所有Exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "服务器错误: " + e.getMessage());
    }
}