package com.zspt.blibli.admin.controller.vo.Comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Data
@Schema(description = "父评论（一级评论）信息")
public  class ParentInfo {
    @Schema(description = "父评论ID（一级评论ID）", example = "1972866931508490241")
    private Long parent_comment_id;

    @Schema(description = "父评论发布人用户ID", example = "1915787134953623554")
    private Long parent_uid;

    @Schema(description = "父评论内容", example = "测试")
    private String parent_content;
}