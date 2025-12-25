package com.zspt.blibli.admin.controller;

import com.zspt.blibli.admin.server.VideoManagementServer;
import com.zspt.blibli.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/videoMan")
@Tag(name="BackVideoController", description = "视频管理")
@Slf4j
public class VideoManagementController {
    @Resource
    VideoManagementServer videoManagementServer;


    @Operation(summary = "获取全部视频信息")
    @GetMapping("/videoInfo")
    public Result allVideoInfo(Integer pageNum) {
        return Result.success( videoManagementServer.ActualizeVideoInfo(pageNum,5));
    }


    @Operation(summary = "根据id获取视频信息")
    @GetMapping("/bIdVideoInfo/{id}")
    public Result bIdVideoInfo(@PathVariable String id) {
        return Result.success(videoManagementServer.ActualizeBIdVideoInfo(Long.parseLong(id)));
    }

    @Operation(summary = "上架视频")
    @PutMapping("/upVideo")
    public Result upVideo(@Parameter(description = "视频ID集合", required = true, example = "[1,2,3]")
                              @RequestBody List<Long> ids) {
        return videoManagementServer.ActualizeUpVideo(ids);
    }

    @Operation(summary = "下架视频")
    @PutMapping("/downVideo")
    public Result downVideo(@Parameter(description = "视频ID集合", required = true, example = "[1,2,3]")
                                @RequestBody List<Long> ids) {
        return videoManagementServer.ActualizeDownVideo(ids);
    }

    @Operation(summary = "删除视频")
    @DeleteMapping("/delVideo")
    public Result delVideo( @Parameter(description = "用户ID集合", required = true, example = "[1,2,3]")
                           @RequestBody List<Long> ids) {
        return videoManagementServer.ActualizeDelVideo(ids);
    }
}
