package com.zspt.blibli.admin.controller;
import com.zspt.blibli.admin.controller.requestParam.BatchCommentStatusParam;
import com.zspt.blibli.admin.server.CommentManagementServer;
import com.zspt.blibli.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/commentMen")
@Slf4j
@Tag(name="BackCommentController", description = "管理端用户评论")
public class CommentManagementController {

    @Resource
    CommentManagementServer commentManagementServer;

    @Operation( summary = "审核评论获取")
    @PostMapping("/getAuditComment")
    public Result login(@RequestParam(defaultValue = "1") Integer pageNum,
                        @RequestParam(required = false) Long videoId )//可选：视频ID，不传查所有
 {
        return  Result.success(commentManagementServer.getAuditCommentByPage(pageNum,videoId));
    }

    @Operation( summary = "审核通过")
    @PostMapping("/batchPass")
    public Result batchPassComment(@Valid @RequestBody BatchCommentStatusParam param) {
       return      commentManagementServer.batchPassComment(param);
    }


    @Operation( summary = "删除/不通过")
    @PostMapping("/batchDelete")
    public Result batchDeleteComment(@Valid @RequestBody BatchCommentStatusParam param) {
    return  commentManagementServer.batchDeleteComment(param);
    }

}



