package com.zspt.blibli.main.exception;

import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import org.springframework.http.HttpStatus;

public class Appexception extends RuntimeException {
    private int code;
    private String msg;
    private HttpStatus httpStatus;
    public Appexception(int code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    public Appexception(AppExceptionCodeMsg appExceptionCodeMsg) {
        super(appExceptionCodeMsg.getMsg());
        this.code = appExceptionCodeMsg.getCode();
        this.msg = appExceptionCodeMsg.getMsg();
        this.httpStatus = appExceptionCodeMsg.getHttpStatus();
    }
    public int getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
