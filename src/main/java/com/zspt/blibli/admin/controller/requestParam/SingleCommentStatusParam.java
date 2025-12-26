package com.zspt.blibli.admin.controller.requestParam;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SingleCommentStatusParam {
    @NotNull(message = "评论层级不能为空")
    @Schema(description = "评论层级：1=一级评论，2=二级评论", example = "1")
    private Integer level;

    @NotNull(message = "评论ID不能为空")
    @Schema(description = "评论ID：一级=comment.id，二级=commentReply.replyId", example = "1111")
    private Long commentId;

}
