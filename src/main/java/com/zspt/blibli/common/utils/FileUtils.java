package com.zspt.blibli.common.utils;

import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FileUtils {

    /**
     * 视频元数据内部类
     */
    private static class VideoMetadata {
        long duration = -1;
        int width = 0;
        int height = 0;
    }

    /**
     * 获取文件后缀名
     */
    public static String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(lastIndex + 1).toLowerCase();
    }

    /**
     * 获取文件夹内部文件个数
     */
    public static int countFiles(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            int count = 0;
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    count++;
                }
            }
            return count;
        }
    }

    /**
     * 生成唯一文件名称
     */
    public static String generateFinalFilename(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

    /**
     * 构建安全的文件路径，防止目录遍历攻击
     */
    public static Path buildSafePath(String filePath, String fileName) {
        // 验证文件名合法性
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            log.error("非法的文件名: {}", fileName);
            throw new Appexception(AppExceptionCodeMsg.INVALID_PATH);
        }

        // 获取的目录
        Path baseDir = Paths.get(filePath).normalize();

        // 安全构建完整路径
        return baseDir.resolve(fileName).normalize();
    }

    /**
     * 获取视频时长（秒）- 优化版
     */
    public static Long getVideoDuration(String videoPath) {
        VideoMetadata metadata = getVideoMetadata(videoPath);
        return metadata != null ? metadata.duration : -1L;
    }

    /**
     * 根据视频尺寸选择合适的分辨率级别 - 优化版
     */
    public static String selectResolution(String videoPath) {
        VideoMetadata metadata = getVideoMetadata(videoPath);
        if (metadata == null) {
            return "1080p"; // 默认改为1080p
        }

        if (metadata.height >= 1080) {
            return "1080p";
        } else if (metadata.height >= 720) {
            return "720p";
        } else {
            return "480p";
        }
    }

    /**
     * 统一获取视频元数据 (减少FFmpeg调用次数)
     */
    private static VideoMetadata getVideoMetadata(String videoPath) {
        try {
            Process process = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath
            ).redirectErrorStream(true).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroy();
                throw new IOException("获取视频元数据超时");
            }

            VideoMetadata metadata = new VideoMetadata();

            // 解析时长
            String[] lines = output.toString().split("\n");
            for (String l : lines) {
                if (l.contains("Duration:")) {
                    String duration = l.split("Duration:")[1].split(",")[0].trim();
                    String[] parts = duration.split(":");
                    metadata.duration = Long.parseLong(parts[0]) * 3600
                            + Long.parseLong(parts[1]) * 60
                            + (long)Double.parseDouble(parts[2]);
                    break;
                }
            }

            // 解析分辨率
            Matcher matcher = Pattern.compile("Stream #.*: Video.*, ([0-9]+)x([0-9]+)").matcher(output.toString());
            if (matcher.find()) {
                metadata.width = Integer.parseInt(matcher.group(1));
                metadata.height = Integer.parseInt(matcher.group(2));
            }

            return metadata;
        } catch (Exception e) {
            log.error("获取视频元数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 使用 FFmpeg 命令行工具将 MP4 文件转换为 M3U8 格式 - 优化版
     */
    public static void convertToM3U8(String mp4FilePath, String m3u8FilePath, int segmentTime) throws Exception {
        File mp4File = new File(mp4FilePath);
        if (!mp4File.exists()) {
            throw new FileNotFoundException("源文件不存在: " + mp4FilePath);
        }

        File m3u8File = new File(m3u8FilePath);
        File parentDir = m3u8File.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("无法创建输出目录: " + parentDir);
        }

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(mp4FilePath);
        command.add("-y");
        command.add("-c:v");
        command.add("libx264");
        command.add("-c:a");
        command.add("aac");
        command.add("-f");
        command.add("hls");
        command.add("-hls_time");
        command.add(String.valueOf(segmentTime));
        command.add("-hls_list_size");
        command.add("0");
        command.add("-hls_segment_filename");
        command.add(parentDir.getAbsolutePath() + "/%03d.ts");
        command.add(m3u8FilePath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            // 只记录错误日志
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[error]") || line.contains("Error")) {
                    log.error(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg 转换失败，退出码: " + exitCode);
            }

            // 验证生成的文件
            if (!m3u8File.exists()) {
                throw new FileNotFoundException("M3U8 文件未生成: " + m3u8FilePath);
            }

            // 兼容方式统计TS文件数量
            File[] tsFiles = parentDir.listFiles((dir, name) -> name.endsWith(".ts"));
            if (tsFiles == null || tsFiles.length == 0) {
                throw new IOException("未生成 TS 切片文件");
            }

            // 转换成功后删除原始文件
            if (mp4File.exists() && !mp4File.delete()) {
                log.warn("无法删除原始 MP4 文件: {}", mp4FilePath);
            }
        } catch (Exception e) {
            // 清理不完整文件
            if (m3u8File.exists()) m3u8File.delete();
            File[] tsFiles = parentDir.listFiles((dir, name) -> name.endsWith(".ts"));
            if (tsFiles != null) {
                for (File tsFile : tsFiles) {
                    tsFile.delete();
                }
            }
            throw e;
        }
    }
}