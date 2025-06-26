package com.zspt.blibli.main.controller;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.controller.requestParam.FileParam;
import com.zspt.blibli.main.server.impl.FileServerImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/file")
public class FileController {
    @Value("${file.upload-path}")
    private String tempDir;
    @Resource
    private FileServerImpl fileServer;

    @PostMapping("/upload")
    public Result postvideo(@ModelAttribute FileParam fileParam) throws IOException {
     return fileServer.upload(fileParam);
    }

    /**
     * 取消上传
     * @param hash
     * @return
     * @throws IOException
     */
    @DeleteMapping("/cancel")
    public Result delFile(@RequestParam("hash") String hash) throws IOException {
        Path chunkDir = Paths.get(tempDir, hash);
            fileServer.deleteFolder(chunkDir);
            return Result.success(null);
    }

    @GetMapping("gethash")
    public Result getHash(@RequestParam("hash") String hash) throws IOException {
       return fileServer.getHash(hash);
    }

    /**
     * 删除上传完成视频
     * @param id
     * @return
     * @throws IOException
     */
    @DeleteMapping("/dlete")
    public Result deleteFile(@RequestParam("id") String id) throws IOException {
     return  fileServer.deleteFile(id);
    }




}
