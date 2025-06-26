package com.zspt.blibli.account.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.account.mapper.domin.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    void updateCoinCount(@Param("videoId") Long videoId, @Param("coinCount") int coinCount);
    int getLikeCount(@Param("userId") Long userId);
}
