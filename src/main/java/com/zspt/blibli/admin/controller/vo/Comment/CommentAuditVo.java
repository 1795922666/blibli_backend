package com.zspt.blibli.admin.controller.vo.Comment;

import lombok.Data;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "评论审核返回类")
public class CommentAuditVo {
    // 公共字段
    @Schema(description = "一级评论ID/二级评论关联的一级评论ID", example = "1992792324264796162")
    private Long comment_id;

    @Schema(description = "评论层级：1=一级评论，2=二级评论", example = "1", allowableValues = {"1", "2"})
    private Integer level;

    @Schema(description = "一级评论：发布人用户ID（二级评论该字段为null）", example = "1915787134953623554")
    private Long pUserId;

    @Schema(description = "一级评论：发布人昵称（二级评论该字段为null）", example = "测试1")
    private String pNickName;

    @Schema(description = "二级评论：发布人用户ID（一级评论该字段为null）", example = "1915787134953623554")
    private Long userId;

    @Schema(description = "二级评论：发布人昵称（一级评论该字段为null）", example = "测试1")
    private String nickName;

    @Schema(description = "评论/回复内容", example = "1111")
    private String content;

    @Schema(description = "创建时间（格式：yyyy-MM-dd HH:mm:ss）", example = "2025-11-24 11:08:09")
    private LocalDateTime create_time;

    @Schema(description = "视频名称（格式：视频《xxx》）", example = "视频《你的名字》")
    private String video;

    @Schema(description = "评论状态：0=待审核，4=被举报", example = "0", allowableValues = {"0", "3"})
    private Byte status;

    // 仅二级评论有
    @Schema(description = "二级评论：被回复人ID（一级评论该字段为null）", example = "1915787134953623554")
    private Long reply_user_id;

    @Schema(description = "二级评论：被回复人昵称（一级评论该字段为null）", example = "测试1")
    private String reply_nick_name;

    // 仅二级评论有（父评论信息）
    @Schema(description = "二级评论：父评论（一级评论）信息（一级评论该字段为null）")
    private ParentInfo parent_info;


}