package com.zspt.blibli.main.server.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.main.mapper.FollowMapper;
import com.zspt.blibli.main.mapper.domin.Follow;
import com.zspt.blibli.main.server.FollowServer;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FollowServerImpl extends ServiceImpl<FollowMapper, Follow> implements FollowServer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 获取当前用户有无关注记录
     * @param followingId
     * @param followerId
     * @return
     */
    @Override
    public Follow followed(Long followingId, Long followerId) {
        return  lambdaQuery().eq(Follow::getFollowingId, followingId).eq(Follow::getFollowerId, followerId).one();
    }



}
