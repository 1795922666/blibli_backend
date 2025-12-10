package com.zspt.blibli.account.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.account.controller.vo.UserInfoVo;
import com.zspt.blibli.account.mapper.domin.User;
import com.zspt.blibli.common.vo.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserServer  extends IService<User> {
    Result login(String username, String password);

    Result getByToken(String tokenValue);

    Result saveUser(User user);

    Result chagePassword(Long id ,String oldPassword,String newPassword);

    ResponseEntity getAvatar(String path) throws IOException;

    Result uploadAvatar(MultipartFile file) throws IOException;

    UserInfoVo getUserInfo(Long id);

    Result followeds(Long id,boolean bol);

    Result followedList(Long id);
}
