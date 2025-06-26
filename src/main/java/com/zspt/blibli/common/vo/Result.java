package com.zspt.blibli.common.vo;

import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    @Schema(description = "响应状态码")
    private int code;

    @Schema(description = "响应消息")
    private String msg;

    @Schema(description = "响应数据")
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> Result success(T data) {
        return  new Result(200,"success",data);
    }
    public static <T> Result success(String msg, T data) {
        return  new Result(200,msg,data);
    }
    public static <T> Result error(AppExceptionCodeMsg appExceptionCodeMsg) {
        return new Result(appExceptionCodeMsg.getCode(),appExceptionCodeMsg.getMsg(),null);
    }

    public static <T> Result error(int code, String msg, T data) {
        return new Result(code,msg,null);
    }
}
