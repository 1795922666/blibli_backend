package com.zspt.blibli.main.config;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FilePathConfig {
    private String filePath;
    private String videoPath;
    private String uploadPath;
    private String coverPath;
    private String avatarPath;

    @PostConstruct
    public void init() {
        createDirectoryIfNotExists(filePath);
        createDirectoryIfNotExists(videoPath);
        createDirectoryIfNotExists(uploadPath);
        createDirectoryIfNotExists(coverPath);
        createDirectoryIfNotExists(avatarPath);
    }

    private void createDirectoryIfNotExists(String dirPath) {
        if (dirPath == null || dirPath.isEmpty()) {
            log.warn("目录路径为空，跳过创建");
            return;
        }

        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                log.info("创建目录: {}", dirPath);
            } catch (IOException e) {
                log.error("创建目录失败: {}", dirPath, e);
                throw new RuntimeException("初始化目录失败: " + dirPath, e);
            }
        }
    }
}