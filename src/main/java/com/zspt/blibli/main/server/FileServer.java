package com.zspt.blibli.main.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.controller.requestParam.FileParam;
import com.zspt.blibli.main.mapper.domin.UpFiles;

import java.io.IOException;
import java.nio.file.Path;

public interface FileServer  extends IService<UpFiles> {

     Result mergeChunks(String id,Long videoId) throws IOException;

      Result getHash(String hash);

     Result deleteFile(String id) throws IOException;

     Result upload(FileParam fileParam) throws IOException;

    void deleteFolder(Path folderPath) throws IOException;
}
