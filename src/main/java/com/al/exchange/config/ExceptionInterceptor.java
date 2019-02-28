package com.al.exchange.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionInterceptor {
    @ExceptionHandler(value = Throwable.class)
    public Object defaultErrorHandler(Throwable e) {
        log.error("全局Controller异常拦截," + e.getMessage(), e);
        return e.getMessage();
    }
}