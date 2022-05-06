package com.project.mentoridge.modules.chat.controller;

import com.project.mentoridge.config.exception.EntityNotFoundException;
import com.project.mentoridge.modules.account.repository.UserRepository;
import com.project.mentoridge.modules.account.vo.User;
import com.project.mentoridge.modules.chat.enums.MessageType;
import com.project.mentoridge.modules.chat.repository.ChatroomRepository;
import com.project.mentoridge.modules.chat.vo.Chatroom;
import com.project.mentoridge.modules.chat.vo.Message;
import com.project.mentoridge.utils.LocalDateTimeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    private Long messageId;
    private MessageType type;
    private Long chatroomId;
    private Long senderId;
    private String text;
    private String createdAt;

    private boolean checked = false;

    public Message toEntity(UserRepository userRepository, ChatroomRepository chatroomRepository) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException(EntityNotFoundException.EntityType.USER));
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new EntityNotFoundException(EntityNotFoundException.EntityType.CHATROOM));
        return Message.builder()
                .type(type)
                .chatroom(chatroom)
                .sender(sender)
                .text(text)
                .build();
    }

    public ChatMessage(Long messageId, MessageType type, Long chatroomId, Long senderId, String text, LocalDateTime createdAt) {
        this.messageId = messageId;
        this.type = type;
        this.chatroomId = chatroomId;
        this.senderId = senderId;
        this.text = text;
        this.createdAt = LocalDateTimeUtil.getDateTimeToString(createdAt);
    }

    public ChatMessage(Message message) {
        this.messageId = message.getId();
        this.type = message.getType();
        this.chatroomId = message.getChatroomId();
        this.senderId = message.getSenderId();
        this.text = message.getText();
        this.createdAt = LocalDateTimeUtil.getDateTimeToString(message.getCreatedAt());
        this.checked = message.isChecked();
    }
}
