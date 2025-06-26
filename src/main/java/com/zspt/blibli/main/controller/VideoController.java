package com.zspt.blibli.main.controller;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.controller.requestParam.ReplyParam;
import com.zspt.blibli.main.controller.requestParam.VideosParam;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import com.zspt.blibli.main.server.impl.VideosServerImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("video")
public class VideoController {
    @Resource
   private VideosServerImpl  videoServerImpl;

    @Operation(summary = "分类获取")
    @GetMapping("category")
    public Result category() {
        return videoServerImpl.getCategories();
    }

    @Operation(summary = "上传视频")
    @PostMapping("uploadVideo")
    public Result uploadVideo(@ModelAttribute VideosParam video) throws IOException {
     return videoServerImpl.addVideos(video);
    }

    @GetMapping("cover")
    public ResponseEntity<FileSystemResource> getcover(@RequestParam("name") String name) throws IOException {
            return  videoServerImpl.getCover( name);
    }

    /**
     * 获取推荐视频列表
     * @return
     */
    @Operation(summary = "推荐视频列表")
    @GetMapping("recommend")
    public Result recommend() {
       return videoServerImpl.getRecommend();
    }

    /**
     * 获取视频m3u8文件
     * @param m3u8Url
     * @return
     * @throws IOException
     */
    @Operation(summary = "获取视频m3u8文件")
    @GetMapping("{m3u8Url}/index.m3u8")
    public ResponseEntity<FileSystemResource> getindex(@PathVariable("m3u8Url") String m3u8Url) throws IOException {
      return   videoServerImpl.getM3u8(m3u8Url);
    }

    @Operation(summary = "获取视频信息")
    @GetMapping("videoInfo/{videId}")
    public Result getVideoInfo(@PathVariable("videId") String videId)  {
        return  videoServerImpl.getVideoInfo(Long.valueOf(videId) );
    }

    /**
     * 点赞
     * @return
     */
    @Operation(summary = "视频点赞")
    @GetMapping("giveLike/{videId}")
    public Result giveLike(@PathVariable("videId") String videId) {
        try {
            return  videoServerImpl.giveLike(Long.valueOf(videId));
        }catch (NumberFormatException e) {
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }
    }

    /**
     * 收藏
     * @return
     */
    @Operation(summary = "视频收藏")
    @GetMapping("collcetVideo/{videId}")
    public Result collcetVideo(@PathVariable("videId") String videId) {
        try {
            return  videoServerImpl.collcet(Long.valueOf(videId));
        }catch (NumberFormatException e) {
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }

    }
    /**
     * 投币
     */
    @Operation(summary = "视频投币")
    @PostMapping("insertCoin")
    public Result insertCoin(@RequestParam("videoId") String videoId,@RequestParam("coinCount") int coinCount) {
        try {
            return  videoServerImpl.insertCoin(Long.valueOf(videoId), coinCount);
        }catch (NumberFormatException e) {
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }
    }

    /**
     * 查询可投币数量
     * @param videId
     * @return
     */
    @Operation(summary = "查询可投币数量")
    @GetMapping("getMaxCoin/{videoId}")
    public Result getMaxCoin(@PathVariable("videoId") String videId) {
    try {
        return  videoServerImpl.numberOfInvestmentsMade(Long.valueOf(videId));
    }catch (NumberFormatException e) {
        throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
    }
    }

    @PutMapping("comment")
    public Result comment(@RequestParam("videoId") String videoId,@RequestParam("content") String content) {
        try {
            return videoServerImpl.comment(Long.valueOf(videoId),content);
        }catch (NumberFormatException e) {
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }
    }

    @PutMapping("reply")
    public Result reply(@RequestBody ReplyParam replyParam) {
        return  videoServerImpl.reply(replyParam);
    }
    @GetMapping("getNewComment")
    public Result getComment(@RequestParam("videoId") String videoId, @RequestParam(value = "lastTime", required = false) String lastTime) {
        try {
            Long vid = Long.valueOf(videoId);
        if (lastTime == null) {
            LocalDateTime time=LocalDateTime.now();
            return  videoServerImpl.getComment(vid,10,time);
        }
        LocalDateTime time=LocalDateTime.parse(lastTime);
        return  videoServerImpl.getComment(vid,10,time);
        }catch (NumberFormatException e) {
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }

    }
}
