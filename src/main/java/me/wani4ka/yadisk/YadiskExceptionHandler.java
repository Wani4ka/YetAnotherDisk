package me.wani4ka.yadisk;

import me.wani4ka.yadisk.exceptions.ApiException;
import me.wani4ka.yadisk.exceptions.ValidationFailedException;
import me.wani4ka.yadisk.models.ApiResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.text.ParseException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class YadiskExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleException(new ValidationFailedException());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleException(new ValidationFailedException());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleException(new ValidationFailedException());
    }

    @ExceptionHandler(ParseException.class)
    protected ResponseEntity<Object> handleParseException() {
        return handleException(new ValidationFailedException());
    }

    @Override
    @ResponseStatus
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(404).body(new ApiResult(404, "Not found"));
    }

    @ExceptionHandler(ApiException.class)
    @ResponseBody
    protected ResponseEntity<Object> handleException(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new ApiResult(e.getStatus().value(), e.getMessage()));
    }
}
