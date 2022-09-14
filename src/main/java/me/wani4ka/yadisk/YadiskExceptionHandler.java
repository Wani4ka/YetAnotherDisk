package me.wani4ka.yadisk;

import me.wani4ka.yadisk.exceptions.InvalidImportException;
import me.wani4ka.yadisk.exceptions.ItemNotFoundException;
import me.wani4ka.yadisk.models.ApiResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class YadiskExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleInvalidImport();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleInvalidImport();
    }

    @ExceptionHandler(InvalidImportException.class)
    protected ResponseEntity<Object> handleInvalidImport() {
        return buildResponseEntity(ApiResult.VALIDATION_FAILED);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    protected ResponseEntity<Object> handleItemNotFound() {
        return buildResponseEntity(ApiResult.NOT_FOUND);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiResult result) {
        return new ResponseEntity<>(result, result.getStatus());
    }
}
