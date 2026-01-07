package com.zspt.blibli.main.server;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.main.mapper.domin.Message;

import java.util.List;

public interface MessageServer extends IService<Message> {
     List<Message> getIncrementChatHistory(Long fromUserId, Long toUserId, Long lastTime);
}
