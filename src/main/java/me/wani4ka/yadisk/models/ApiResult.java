package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

public class ApiResult {
    public static final ApiResult OK = new ApiResult(HttpStatus.OK);
    public static final ApiResult VALIDATION_FAILED = new ApiResult(HttpStatus.BAD_REQUEST, "Validation Failed");
    public static final ApiResult NOT_FOUND = new ApiResult(HttpStatus.NOT_FOUND, "Item not found");

    @JsonIgnore
    private final HttpStatus status;
    private final int code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    public ApiResult(HttpStatus status, String message) {
        this.status = status;
        this.code = status.value();
        this.message = message;
    }

    public ApiResult(HttpStatus status) {
        this(status, null);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
