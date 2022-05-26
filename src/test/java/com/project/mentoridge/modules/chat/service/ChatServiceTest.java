package com.project.mentoridge.modules.chat.service;

import com.project.mentoridge.modules.account.repository.MenteeRepository;
import com.project.mentoridge.modules.account.repository.MentorRepository;
import com.project.mentoridge.modules.account.repository.UserRepository;
import com.project.mentoridge.modules.account.vo.User;
import com.project.mentoridge.modules.chat.repository.ChatroomRepository;
import com.project.mentoridge.modules.chat.repository.MessageMongoRepository;
import com.project.mentoridge.modules.chat.vo.Chatroom;
import com.project.mentoridge.modules.log.component.ChatroomLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static com.project.mentoridge.config.init.TestDataBuilder.getUserWithName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    ChatService chatService;

    @Mock
    ChatroomRepository chatroomRepository;
    @Mock
    MenteeRepository menteeRepository;
    @Mock
    MentorRepository mentorRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    MessageMongoRepository messageRepository;
    @Mock
    ChatroomLogService chatroomLogService;
//
//    @DisplayName("채팅 신청")
//    @Test
//    void new_chatroom() {
//        // 멘티(로그인)가 멘토에게 채팅 신청
//        // given
//        User menteeUser = getUserWithNameAndRole("mentee", RoleType.MENTEE);
//        Mentee mentee = Mentee.builder()
//                .user(menteeUser)
//                .build();
//        when(menteeRepository.findByUser(menteeUser)).thenReturn(mentee);
//        User mentorUser = getUserWithNameAndRole("mentor", RoleType.MENTOR);
//        Mentor mentor = Mentor.builder()
//                .user(mentorUser)
//                .bio("hello")
//                .build();
//        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
//
//        Chatroom chatroom = mock(Chatroom.class);
//        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(chatroom);
//
//        // when
//        chatService.createChatroomByMentee(menteeUser, 1L);
//        // then
//        verify(chatroomRepository).save(any(Chatroom.class));
//        // assertThat(WebSocketHandler.chatroomMap.get(1L)).isNotNull();
//    }
    // TODO - PrincipalDetails로 변경

    @DisplayName("채팅 종료")
    @Test
    void close_chatroom() {
        // chatroomId

        // given
        Chatroom chatroom = mock(Chatroom.class);
        // when(chatroom.getId()).thenReturn(1L);
        when(chatroomRepository.findById(1L)).thenReturn(Optional.of(chatroom));
        // WebSocketHandler.chatroomMap.put(1L, new HashMap<>());

        // when
        // TODO - user
        User user = getUserWithName("user");
        chatService.closeChatroom(user, 1L);

        // then
        verify(chatroom).close();
    }

    @Test
    void accuse() {
    }
}