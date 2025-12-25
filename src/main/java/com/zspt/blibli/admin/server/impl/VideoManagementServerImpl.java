package com.zspt.blibli.admin.server.impl;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.admin.server.VideoManagementServer;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import com.zspt.blibli.main.mapper.VideosMapper;
import com.zspt.blibli.main.mapper.domin.Videos;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class VideoManagementServerImpl extends ServiceImpl<VideosMapper, Videos> implements VideoManagementServer {
    @Override
    public IPage<Videos> ActualizeVideoInfo(Integer pageNum, Integer pageSize) {
        // 1. 构建分页对象：Page<实体类>(页码, 每页条数)
        Page<Videos> page = new Page<>(pageNum, pageSize);

        // 2. 执行分页查询
        IPage<Videos> adminPage =this.getBaseMapper().selectPage(page, null);
        return  adminPage ;
    }

    @Operation
    public Videos ActualizeBIdVideoInfo(long id){
         Videos video = this.getBaseMapper().selectById(id);
         if(video == null)throw  new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
         return  video;
    }

    @Operation
    public Result  ActualizeUpVideo(List<Long> ids){
        // 1.双层非空 / 有效性校验
        if (CollectionUtil.isEmpty(ids)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }
        if (ids.contains(null)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }

        // 2.校验ID对应的用户是否存在
        long existCount = lambdaQuery().in(Videos::getVideoId, ids).count();
        if (existCount != ids.size()) {
            log.warn("上架视频失败：部分ID不存在，请求ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }


        // 逻辑删除（推荐，更新status为禁用）
        boolean deleteSuccess = lambdaUpdate().in(Videos::getVideoId, ids).set(Videos::getStatus, 1).set(Videos::getUpdatedAt, new Date()) .update();

        if (!deleteSuccess) {
            log.error("上架视频失败：ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }

        log.info("上架视频失败：共下架{}个视频，ID={}", ids.size(), ids);
        return Result.success("上架成功，共上架" + ids.size() + "个视频");

    }

    @Operation
    public Result  ActualizeDownVideo(List<Long> ids){
        // 1.双层非空 / 有效性校验
        if (CollectionUtil.isEmpty(ids)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }
        if (ids.contains(null)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }

        // 2.校验ID对应的用户是否存在
        long existCount = lambdaQuery().in(Videos::getVideoId, ids).count();
        if (existCount != ids.size()) {
            log.warn("下架视频失败：部分ID不存在，请求ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }


        // 逻辑删除（推荐，更新status为禁用）
        boolean deleteSuccess = lambdaUpdate().in(Videos::getVideoId, ids).set(Videos::getStatus, 2).set(Videos::getUpdatedAt, new Date()) .update();

        if (!deleteSuccess) {
            log.error("下架视频失败：ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }

        log.info("下架视频失败：共下架{}个视频，ID={}", ids.size(), ids);
        return Result.success("下架成功，共下架" + ids.size() + "个视频");

    }

    @Operation
    public Result  ActualizeDelVideo(List<Long> ids){
        // 1.双层非空 / 有效性校验
        if (CollectionUtil.isEmpty(ids)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }
        if (ids.contains(null)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }

        // 2.校验ID对应的用户是否存在
        long existCount = lambdaQuery().in(Videos::getVideoId, ids).count();
        if (existCount != ids.size()) {
            log.warn("删除视频失败：部分ID不存在，请求ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }


        // 逻辑删除（推荐，更新status为禁用）
        boolean deleteSuccess = lambdaUpdate().in(Videos::getVideoId, ids).set(Videos::getStatus, 3).set(Videos::getUpdatedAt, new Date()).update();

        if (!deleteSuccess) {
            log.error("删除视频失败：ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
        }

        log.info("删除视频成功：共删除{}个视频，ID={}", ids.size(), ids);
        return Result.success("删除成功，共删除" + ids.size() + "个视频");

    }
}
