package com.zspt.blibli.main.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.mapper.FollowMapper;
import com.zspt.blibli.main.mapper.domin.Follow;

public interface FollowServer extends IService<Follow> {
    Follow followed(Long followingId, Long followerId);

}
