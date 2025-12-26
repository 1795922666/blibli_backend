package com.zspt.blibli.dynamic.server.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.dynamic.mapper.DynamicPictureMapper;
import com.zspt.blibli.main.config.FilePathConfig;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.zspt.blibli.dynamic.mapper.domin.DynamicPicture;
import com.zspt.blibli.dynamic.server.DynamicPictureServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DynamicPictureServerImpl extends ServiceImpl<DynamicPictureMapper,DynamicPicture> implements DynamicPictureServer {

    @Resource
    private DynamicPictureMapper dynamicPictureMapper;

    @Resource
    private FilePathConfig filePathConfig;


    // 后台服务的域名/端口（可以配置在application.yml中）
    @Value("${server.address:localhost}")
    private String serverAddress;
    @Value("${server.port:4000}")
    private String serverPort;

    // 组装图片访问的基础URL
    private String getPictureBaseUrl() {
        return "/api/dynamic/picture/";
    }


    @Override
    public Result addPicture(DynamicPicture dynamicPicture) {

        this.saveOrUpdate(dynamicPicture);

        return Result.success("上传成功");
    }

    @Override
    public List<DynamicPicture> queryByNonPrimaryIds(List<Long> userIdList) {
        if(userIdList.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DynamicPicture> queryWrapper = Wrappers.lambdaQuery(DynamicPicture.class)
                // 核心：in(非主键字段, ID列表)
                .in(DynamicPicture::getDynamicId, userIdList)
                .orderByDesc(DynamicPicture::getCreateTime);

        // 2. 执行查询，返回符合条件的所有数据
        ArrayList<DynamicPicture> dynamicPictures = new ArrayList<>(dynamicPictureMapper.selectList(queryWrapper));
        // 拼接完整


        return dynamicPictures.stream().map(e -> {
            DynamicPicture dynamicPicture = e;
            // 拼接完整
            String pictureUrl = getPictureBaseUrl() + dynamicPicture.getPictureUrl();
            dynamicPicture.setPictureUrl(pictureUrl);
            return dynamicPicture;
        }).toList();

    }

    public void getPicture( String fileName, HttpServletResponse response) {

    }

}
