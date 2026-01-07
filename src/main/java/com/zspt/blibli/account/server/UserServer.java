package com.zspt.blibli.account.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.account.controller.vo.UserInfoVo;
import com.zspt.blibli.account.mapper.domin.User;
import com.zspt.blibli.common.vo.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

public interface UserServer  extends IService<User> {
    Result login(String username, String password, HttpSession session);

    Result getByToken(String tokenValue,HttpSession session);

    Result saveUser(User user);

    Result chagePassword(Long id ,String oldPassword,String newPassword);

    ResponseEntity getAvatar(String path) throws IOException;

    Result uploadAvatar(MultipartFile file) throws IOException;

    String  updateAvatar(String id,String path);

    UserInfoVo getUserInfo(Long id);

    Result followeds(Long id,boolean bol);

    Result followedList(Long id);

    long countNewUsersByTimeRange(Date startTime, Date endTime);
}
