package com.zspt.blibli.dynamic.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.dynamic.controller.vo.DynamicCommentVo;
import com.zspt.blibli.dynamic.controller.vo.DynamicInfoVo;
import com.zspt.blibli.dynamic.controller.vo.DynamicVo;
import com.zspt.blibli.dynamic.mapper.domin.Dynamic;
import com.zspt.blibli.dynamic.server.impl.DynamicServerImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/dynamic")
@Slf4j
public class DynamicController {



    @Resource
    private DynamicServerImpl dynamicServer;

    @Operation(summary = "发布动态")
    @PutMapping("/add")
    public Result ActiveServerPage(@RequestParam("Dynamic") String  dynamicVo, @RequestParam(value = "files",required = false) List<MultipartFile> files) {
        log.info("发布动态");
        if(files == null) {
            files = new ArrayList<>();
        }
        return    dynamicServer.addDynamic(dynamicVo,files);
    }

    @Operation(summary = "全部动态")
    @GetMapping("/AllInfo")
    public Result<List<DynamicInfoVo>> dynamicAllInfo(@RequestParam("num") Integer num) {
        List<DynamicInfoVo> dynamicByPage = dynamicServer.getDynamicByPage(num);
        return Result.success(dynamicByPage);
    };

    @Operation(summary = "动态点赞")
    @PostMapping("/{id}/like")
    public Result dynamicLike( @PathVariable  Long id,@RequestParam("status") byte status)  {

        return dynamicServer.dynamicLike(id,status);
    }

    @Operation(summary = "动态评论")
    @PostMapping("/comment")
    public Result dynamicComment(@RequestBody DynamicCommentVo dynamicCommentVo) {


        return dynamicServer.dynamicComment(dynamicCommentVo);
    }

    @Operation(summary = "动态评论点赞",description = "status 0 表示取消 1 点赞")
    @PostMapping("/comment/{id}/like")
    public Result dynamicCommentLike(@RequestParam("userId") Long userid,@RequestParam("commentId") Long commentId,@RequestParam("status") byte status){

        return dynamicServer.dynamicCommentLike(userid,commentId,status);
    }

    @Operation(summary = "动态转发" )
    @PostMapping("/transmit")
    public Result dynamicTransmit(@RequestParam("userId")Long userId,@RequestParam("dynamicId") Long dynamicId,@RequestParam("description") String description) {
        return dynamicServer.dynamicTransmit(userId,dynamicId,description);
    }

    @Operation(summary = "动态转发点赞" )
    @PostMapping("/transmit/{id}/like")
    public Result dynamicTransmitLike(@RequestParam("dynamicId") Long dynamicId,@RequestParam("status") byte status) {
        return dynamicServer.dynamicTransmitLike(dynamicId,status);
    }

    @Operation(summary = "评论列表")
    @GetMapping("/comment/list/{id}")
    public Result dynamicCommentList(@PathVariable Long id) {
        return dynamicServer.dynamicCommentList(id);
    }

    @Operation(summary = "转发列表")
    @GetMapping("/transmit/list/{id}")
    public Result dynamicTransmitList(@PathVariable Long id) {
        return dynamicServer.dynamicTransmitList(id);
    }

    @Operation(summary = "动态审核表")
    @GetMapping("/audit/list")
    public Result dynamicAuditList() {
        return dynamicServer.dynamicCauditList();
    }

    @Operation(summary = "动态审核")
    @PostMapping("/audit/{id}")
    public Result dynamicAuditList(@PathVariable Long id,@RequestParam("status") byte status) {
        return dynamicServer.dynamiCaudit(id,status);
    }

}
