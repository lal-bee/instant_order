package fun.cyhgraph.handler;

import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，能对项目中抛出的异常进行捕获和处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result excepitonHandler(BaseException ex){
        log.info("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result exceptionHandler(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        Throwable cause = ex.getCause();
        if (cause != null) message = cause.getMessage();
        log.warn("数据库约束异常: {}", message);
        if (message != null && message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String value = split.length > 2 ? split[2] : "";
            return Result.error(value + MessageConstant.ALREADY_EXiST);
        }
        if (message != null && message.contains("cannot be null")) {
            return Result.error("数据约束错误: " + message);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        log.warn("SQL 约束异常: {}", message);
        if (message != null && message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String username = split.length > 2 ? split[2] : "";
            return Result.error(username + MessageConstant.ALREADY_EXiST);
        }
        if (message != null && message.contains("cannot be null")) {
            return Result.error("数据约束: " + message);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
