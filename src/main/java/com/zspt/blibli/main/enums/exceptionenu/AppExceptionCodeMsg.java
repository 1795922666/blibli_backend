package com.zspt.blibli.main.enums.exceptionenu;

import org.springframework.http.HttpStatus;

public enum AppExceptionCodeMsg {
    // 认证相关错误 (401)
    AUTH_FAILED(40100, "用户或密码错误", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(40101, "Token无效", HttpStatus.UNAUTHORIZED),
    LOGIN_EXPIRED(40102, "登录已过期", HttpStatus.UNAUTHORIZED),
    DATA_CONFLICT(40000, "修改失败", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(40401, "用户不存在", HttpStatus.NOT_FOUND),
    USER_STATUS_CLOSURE(40300, "用户封禁", HttpStatus.NOT_FOUND),
    USER_DELETE_FAILED(40301,"用户注销失败",HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS(40900, "用户已存在", HttpStatus.CONFLICT),
    USER_AVATAR_NOT_FOUND(20003, "用户头像不存在", HttpStatus.NOT_FOUND),
    USER_AVATAR_ACCESS_DENIED(20004, "无权限访问头像", HttpStatus.FORBIDDEN),
    INVALID_PATH(20005, "无效路径", HttpStatus.BAD_REQUEST),
    NOT_LOGGED_IN(40103, "未登录", HttpStatus.UNAUTHORIZED),
    NOT_NULL(40300, "不能为空", HttpStatus.NOT_FOUND),

    // 文件上传相关错误 (400/500)
    // 文件相关错误 (404)
    FILE_NOT_FOUND(40402, "文件不存在", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED(40001, "文件上传失败", HttpStatus.BAD_REQUEST),
    FILE_INCOMPLETE(40002, "文件上传不完整", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(40003, "文件大小超过限制", HttpStatus.BAD_REQUEST),
    FILE_EMPTY(40004, "上传文件为空", HttpStatus.BAD_REQUEST),
    FILE_EXISTS(40006, "文件已存在", HttpStatus.BAD_REQUEST),
    FILE_TYPE_INVALID(40005, "文件类型不支持", HttpStatus.BAD_REQUEST),
    FILE_STORAGE_ERROR(50001, "文件存储失败", HttpStatus.INTERNAL_SERVER_ERROR),

    VIDEO_EXISTS(40007, "视频已存在", HttpStatus.BAD_REQUEST),
    VIDEO_NOT_FOUND(40402, "视频不存在", HttpStatus.NOT_FOUND),
    VIDEO_UPLOAD_FAILED(40008, "视频上传失败", HttpStatus.BAD_REQUEST),
    VIDEO_COVER_NOT_FOUND(20005, "视频封面不存在", HttpStatus.NOT_FOUND),
    VIDEO_PROCESSING_FAILED(50002, "视频处理失败", HttpStatus.INTERNAL_SERVER_ERROR),


    COIN_AMOUNT_INVALID(20001, "投币金额不合法", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_FOUND(20101, "评论不存在", HttpStatus.BAD_REQUEST),
    COMMENT_USER_NOT_FOUND(20102, "评论用户不存在", HttpStatus.NOT_FOUND),
    OPERATION_TOO_FREQUENT(42900, "操作过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS),
    PARAM_ERROR(40002, "参数错误", HttpStatus.BAD_REQUEST);


    ;

    private final int code;         // 业务错误子码
    private final String msg;   // 错误描述
    private final HttpStatus httpStatus; // 关联的HTTP状态码


    AppExceptionCodeMsg(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.msg = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public String getMsg() { return msg; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}