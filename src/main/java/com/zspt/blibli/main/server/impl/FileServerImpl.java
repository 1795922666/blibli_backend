package com.zspt.blibli.main.server.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.common.utils.FileUtils;
import com.zspt.blibli.common.utils.SystemConstants;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.controller.requestParam.FileParam;
import com.zspt.blibli.main.dto.FileUploadDTO;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import com.zspt.blibli.main.mapper.FileMapper;
import com.zspt.blibli.main.mapper.domin.UpFiles;
import com.zspt.blibli.main.server.FileServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FileServerImpl extends ServiceImpl<FileMapper, UpFiles> implements FileServer {
    @Value("${file.upload-path}")
    private String tempDir;
    @Value("${file.file-path}")
    private String filePath;
    @Value("${file.video-path}")
    private String VideoPath;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Result upload(FileParam fileParam) throws IOException {
        int index= fileParam.getIndex();
        int total = fileParam.getTotal();
//        生成上传文件文件夹
        String path =fileParam.getUid();
        Path chunkDir  = Paths.get(tempDir,path);
        Files.createDirectories(chunkDir);

        String chunkFilename = SystemConstants.CHUNK + index;
        Path chunkPath = chunkDir.resolve(chunkFilename);
        fileParam.getFile().transferTo(chunkPath);

        //文件上传完成
        if(index+1==total){
            FileUploadDTO fileUploadDTO = new FileUploadDTO();
            long epochSecond = Instant.now().plus(24, ChronoUnit.HOURS).getEpochSecond();

            fileUploadDTO.setTotal(String.valueOf(total));
            fileUploadDTO.setHash(fileParam.getHash());
            fileUploadDTO.setExpire_time(String.valueOf(epochSecond));
            fileUploadDTO.setStatus("0");
            fileUploadDTO.setFileUrl(chunkDir.toString());

            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(fileUploadDTO);

            stringRedisTemplate.opsForHash().putAll(SystemConstants.REDIS_FILE_UPLOAD+fileParam.getUid(),stringObjectMap);
        }
        return Result.success(null);
    }



    /**
 * 合并视频分片并转码为M3U8格式
 * @param id
 * @param videoId
 * @return 返回操作结果（包含文件ID）
 * @throws IOException 文件操作异常
 */

    @Override
    public Result mergeChunks(String id,Long videoId) throws IOException {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(SystemConstants.REDIS_FILE_UPLOAD +id);
        if(entries.isEmpty()){
            throw new Appexception(AppExceptionCodeMsg.FILE_NOT_FOUND);
        }
        FileUploadDTO fileUploadDTO = BeanUtil.mapToBean(entries, FileUploadDTO.class, false, new CopyOptions());

        int total=Integer.parseInt(fileUploadDTO.getTotal());
        String hash= fileUploadDTO.getHash();
        // 1. 校验分片完整性
        Path chunkDir = Paths.get(tempDir, id );
        int actualChunkCount = FileUtils.countFiles(chunkDir);
        if (actualChunkCount != total) {
            log.error("分片数量不匹配，期望: {}，实际: {}", total, actualChunkCount);
            throw new Appexception(AppExceptionCodeMsg.FILE_INCOMPLETE);
        }

        // 2. 生成最终文件名（MP4文件）
        String tempFilename = hash + ".mp4";
        Path tempMp4Path = Paths.get(filePath, tempFilename);

        // 3. 合并分片MP4文件
        try (FileChannel destChannel = FileChannel.open(tempMp4Path,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

            for (int i = 0; i < total; i++) {
                Path chunkPath = chunkDir.resolve(SystemConstants.CHUNK + i);
                try (FileChannel srcChannel = FileChannel.open(chunkPath, StandardOpenOption.READ)) {
                    srcChannel.transferTo(0, srcChannel.size(), destChannel);
                }
                Files.delete(chunkPath); // 立即删除分片
            }
            log.info("分片合并完成，临时MP4文件：{}", tempMp4Path);
        }

        Long videoDuration = FileUtils.getVideoDuration(tempMp4Path.toString());
        long size = Files.size(tempMp4Path);
        // 4. 创建HLS专属目录（格式：文件哈希值_hls）
        String hlsDirName = UUID.randomUUID().toString();
        Path hlsDir = Paths.get(VideoPath, hlsDirName);
        Files.createDirectories(hlsDir);

// 5. 将MP4文件转换为M3U8格式
       String  m3u8Name="index";
        Path m3u8FilePath = hlsDir.resolve(m3u8Name+".m3u8");

        UpFiles upFiles = new UpFiles();
//        关联视频id
        upFiles.setVideoId(videoId);
//        设置hash值
        upFiles.setFileHash(hash);
//        m3u8文件名
        upFiles.setFileName(m3u8Name);
//        视频时长
        upFiles.setDuration(videoDuration);
//        视频大小
        upFiles.setFileSize(size);
//        视频文件文件夹
        upFiles.setFileUrl(hlsDirName);


        upFiles.setFormat("m3u8");
        try {
            // 调用转换方法
            FileUtils.convertToM3U8(tempMp4Path.toString(), m3u8FilePath.toString(),10);
            log.info("MP4转M3U8完成，输出目录：{}", hlsDir);
//            保存视频文件信息
            save(upFiles);
            stringRedisTemplate.opsForHash().put(SystemConstants.REDIS_FILE_UPLOAD + id,"status","1");
            // 9. 清理空目录
            return Result.success(null);
        } catch (Exception e) {
            log.error("MP4转M3U8失败", e);
            Files.deleteIfExists(hlsDir);
            // 删除已创建的HLS目录（使用新增的工具方法）
            throw new Appexception(AppExceptionCodeMsg.FILE_UPLOAD_FAILED);
        }finally {
            Files.deleteIfExists(chunkDir);
            Files.deleteIfExists(tempMp4Path); // 删除临时文件
        }

    }

    @Override
    public Result getHash(String hash) {
        boolean exists = lambdaQuery().eq(UpFiles::getFileHash, hash).exists();
        return Result.success(exists);
    }

    @Override
    public Result deleteFile(String id) throws IOException {
        Path finalPath = Paths.get(tempDir, id);
        deleteFolder(finalPath);
        stringRedisTemplate.delete(SystemConstants.REDIS_FILE_UPLOAD+id);
        return Result.success(null);
    }

    @Override
    public void deleteFolder(Path folderPath) throws IOException {
        if (Files.isDirectory(folderPath)) {
            Files.list(folderPath).forEach(file -> {
                try {
                    if (Files.isDirectory(file)) {
                        deleteFolder(file);
                    } else {
                        Files.delete(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        Files.delete(folderPath);
    }


}
