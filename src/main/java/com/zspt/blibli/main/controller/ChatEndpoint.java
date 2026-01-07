package com.zspt.blibli.main.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zspt.blibli.main.config.GetHttpSessionConfigurator;
import com.zspt.blibli.main.controller.requestParam.MessageParam;
import com.zspt.blibli.main.controller.vo.MessageVo;
import com.zspt.blibli.main.dto.RuMessageDTO;
import com.zspt.blibli.main.mapper.domin.Message;
import com.zspt.blibli.main.server.MessageServer;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfigurator.class)
@Component
@Slf4j
public class ChatEndpoint {

    // 保留你原来的静态注入方式
    private static MessageServer messageServer;

    @Autowired
    public void setMessageServer(MessageServer messageServer) {
        ChatEndpoint.messageServer = messageServer;
    }

    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    /**
     * 建立连接后调用
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 1. 从 config 中获取 HttpSession
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        if (httpSession == null) {
            log.error("WebSocket 连接失败：无法获取 HttpSession。");
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "HttpSession is required."));
            } catch (Exception e) {
                log.error("关闭会话失败", e);
            }
            return;
        }

        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) {
            log.error("WebSocket 连接失败：HttpSession 中未找到 userId。");
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User not logged in."));
            } catch (Exception e) {
                log.error("关闭会话失败", e);
            }
            return;
        }

        // 2. 将 userId 存入 Session 的用户属性中，而不是实例变量
        session.getUserProperties().put("userId", userId);

        // 3. 将用户添加到在线列表
        onlineUsers.put(userId, session);
        log.info("用户 {} 连接成功，当前在线用户数：{}", userId, onlineUsers.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来自会话 {} 的消息：{}", session.getId(), message);

        // 1. 从 Session 属性中获取当前用户ID，这是线程安全的
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId == null) {
            log.error("无法从 Session 中获取用户ID，消息处理中断。");
            return;
        }

        try {
            MessageParam mess = JSONUtil.toBean(message, MessageParam.class);
            if ("chat".equals(mess.getType())) {
                Long toUserId = mess.getData().getToUserId();
                Session toUserSession = onlineUsers.get(toUserId);

                Message vo = new Message();
                BeanUtils.copyProperties(mess.getData(), vo);
                vo.setCreatedAt(mess.getCreatedAt());
                vo.setFromUserId(userId); // 使用从 Session 获取的 userId

                if (messageServer != null) {
                    messageServer.save(vo);
                } else {
                    log.error("messageServer is null, cannot save message");
                }

                if (toUserSession != null && toUserSession.isOpen()) {
                    MessageVo mv = new MessageVo();
                    RuMessageDTO mdto = new RuMessageDTO();
                    BeanUtils.copyProperties(vo, mdto);
                    mdto.setFromUserId(userId.toString());

                    mv.setData(mdto);
                    mv.setCreatedAt(mess.getCreatedAt());
                    mv.setType("chat");
                    String jsonStr = JSONUtil.toJsonStr(mv);
                    toUserSession.getAsyncRemote().sendText(jsonStr);
                    log.info("消息发送给用户 {}: {}", toUserId, jsonStr);
                } else {
                    log.info("用户 {} 不在线，消息已存入数据库。", toUserId);
                }

            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        // 从 Session 属性中安全地获取 userId
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            onlineUsers.remove(userId);
            log.info("用户 {} 断开连接，原因: {}, 当前在线用户数：{}", userId, closeReason, onlineUsers.size());
        } else {
            log.warn("会话 {} 断开连接，未关联用户ID。", session.getId());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        // 从 Session 属性中安全地获取 userId
        Long userId = (Long) session.getUserProperties().get("userId");
        log.error("WebSocket 连接出错，用户ID：{}，会话ID：{}，错误信息：{}", userId, session.getId(), error.getMessage(), error);
        // 出错后，确保用户从在线列表中移除
        if (userId != null) {
            onlineUsers.remove(userId);
        }
    }
}