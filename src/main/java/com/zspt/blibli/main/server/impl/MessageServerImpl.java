package com.zspt.blibli.main.server.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.main.mapper.MessageMapper;
import com.zspt.blibli.main.mapper.domin.Message;
import com.zspt.blibli.main.server.MessageServer;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class MessageServerImpl extends ServiceImpl<MessageMapper, Message> implements MessageServer {

        @Override
        public List<Message> getIncrementChatHistory(Long contentId, Long toUserId, Long lastTime) {
            if (contentId == null || toUserId == null || lastTime == null) {
                return Collections.emptyList();
            }
            // 3. 构造查询条件：仅查「对方发的、自己收的、时间>lastTime、状态=0」的消息
            LambdaQueryWrapper<Message> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Message::getFromUserId, contentId) // 接收者=自己
                    .eq(Message::getToUserId, toUserId) // 发送者=对方
                    .gt(Message::getCreatedAt, lastTime)     // 时间>本地最后一条
                    .orderByAsc(Message::getCreatedAt);      // 按时间正序
            // 4. 执行查询（仅返回对方发给自己的增量消息）
            return  this.baseMapper.selectList(wrapper);
        }



}
