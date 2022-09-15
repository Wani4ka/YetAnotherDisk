package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class ApiResult {
    public static final ApiResult OK = new ApiResult(200, null);

    private final int code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;
}
