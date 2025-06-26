package com.zspt.blibli.main.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.zspt.blibli.common.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public <T> Result<T> exceptionHandler(Exception e,  HttpServletResponse response){
        if(e instanceof Appexception){
            Appexception appexception = (Appexception)e;
            response.setStatus(appexception.getHttpStatus().value());
            return Result.error(appexception.getCode(),appexception.getMsg(),null);
        }
        response.setStatus(500);
        e.printStackTrace();
        return Result.error(500,"服务器错误",null);
    }


}
