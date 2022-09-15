package me.wani4ka.yadisk.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationFailedException extends ApiException {
    public ValidationFailedException() {
        super("Validation Failed");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
