package com.zspt.blibli.main.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.controller.requestParam.ReplyParam;
import com.zspt.blibli.main.controller.requestParam.VideosParam;
import com.zspt.blibli.main.controller.vo.VideoStatus;
import com.zspt.blibli.main.mapper.domin.Videos;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

public interface VideosServer extends IService<Videos> {
    Result addVideos( VideosParam videosParam) throws IOException;

    Result getCategories();

    ResponseEntity<FileSystemResource> getCover(String coverUrl) throws IOException;

   Result getRecommend();

   ResponseEntity<FileSystemResource> getM3u8(String m3u8Url) throws IOException;

    Result getVideoInfo(Long videId);

    VideoStatus getInteractionStatus(Long videoId, Long userId);

    Result giveLike(Long videId);

    Result collcet(Long videId);

    Result insertCoin(Long videId,int coinCount);

    Result numberOfInvestmentsMade(Long videoId);

    Result comment(Long videoId, String content);

    Result reply(ReplyParam replyParam);

    Result getComment(Long videoId, int pageSize, LocalDateTime lastTime);

    Result getCommentReply(Long commentId, int pageSize, LocalDateTime lastTime);

    long countNewVideosByTimeRange(Date startTime, Date endTime);
}
