package com.zspt.blibli.dynamic.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.common.vo.Result;

import com.zspt.blibli.dynamic.controller.vo.DynamicCommentVo;
import com.zspt.blibli.dynamic.controller.vo.DynamicVo;
import com.zspt.blibli.dynamic.mapper.domin.Dynamic;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public interface DynamicServer extends IService<Dynamic> {

    Result addDynamic(@Param("dynamicInfo") String dynamic, List<MultipartFile> files); //写动态
    Result deleteDynamic(@Param("dynamicID") Long id);
    Result dynamicAllInfo(Integer num ); // 全部动态
    Result dynamicSingleInfo(@Param("userId") Long userId,Integer num); // 单个关注者的动态


    Result dynamicLike(Long id,byte status);

    Result dynamicComment(DynamicCommentVo dynamicCommentVo);

    Result dynamicCommentLike(Long userid, Long commentId,byte status);

    Result dynamicTransmit(Long userId, Long dynamicId, String description);

    Result dynamicTransmitLike( Long transmitId, byte status);

    Result dynamicCommentList(Long id);

    Result dynamicTransmitList(Long id);
}
