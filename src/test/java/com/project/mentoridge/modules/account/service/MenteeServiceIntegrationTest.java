package com.project.mentoridge.modules.account.service;

import com.project.mentoridge.modules.account.controller.response.MenteeResponse;
import com.project.mentoridge.modules.account.enums.RoleType;
import com.project.mentoridge.modules.account.repository.MenteeRepository;
import com.project.mentoridge.modules.account.repository.UserRepository;
import com.project.mentoridge.modules.account.vo.Mentee;
import com.project.mentoridge.modules.account.vo.User;
import com.project.mentoridge.modules.chat.repository.ChatroomRepository;
import com.project.mentoridge.modules.purchase.repository.EnrollmentRepository;
import com.project.mentoridge.modules.purchase.repository.PickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.project.mentoridge.configuration.AbstractTest.menteeUpdateRequest;
import static com.project.mentoridge.modules.account.controller.IntegrationTest.saveMenteeUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class MenteeServiceIntegrationTest {

    @Autowired
    LoginService loginService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MenteeService menteeService;
    @Autowired
    MenteeRepository menteeRepository;

    @Autowired
    ChatroomRepository chatroomRepository;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    private User menteeUser;
    private Mentee mentee;

    @BeforeEach
    void init() {

        menteeUser = saveMenteeUser(loginService);
        mentee = menteeRepository.findByUser(menteeUser);
    }

    @Test
    void get_MenteeResponse() {

        // Given
        // When
        MenteeResponse response = menteeService.getMenteeResponse(mentee.getId());
        // Then
        assertAll(
                () -> assertThat(response).extracting("user").extracting("userId").isEqualTo(menteeUser.getId()),
                () -> assertThat(response).extracting("user").extracting("username").isEqualTo(menteeUser.getUsername()),
                () -> assertThat(response).extracting("user").extracting("role").isEqualTo(menteeUser.getRole()),
                () -> assertThat(response).extracting("user").extracting("name").isEqualTo(menteeUser.getName()),
                () -> assertThat(response).extracting("user").extracting("gender").isEqualTo(menteeUser.getGender().name()),
                () -> assertThat(response).extracting("user").extracting("birthYear").isEqualTo(menteeUser.getBirthYear()),
                () -> assertThat(response).extracting("user").extracting("phoneNumber").isEqualTo(menteeUser.getPhoneNumber()),
                () -> assertThat(response).extracting("user").extracting("nickname").isEqualTo(menteeUser.getNickname()),
                () -> assertThat(response).extracting("user").extracting("image").isEqualTo(menteeUser.getImage()),
                () -> assertThat(response).extracting("user").extracting("zone").isEqualTo(menteeUser.getZone().toString()),
                () -> assertThat(response).extracting("subjects").isEqualTo(mentee.getSubjects())
        );
    }

    @Test
    void update_mentee() {

        // Given
        // When
        menteeService.updateMentee(menteeUser, menteeUpdateRequest);

        // Then
        Mentee mentee = menteeRepository.findByUser(menteeUser);
        assertAll(
                () -> assertNotNull(mentee),
                () -> {
                    assert menteeUser != null;
                    assertEquals(RoleType.MENTEE, menteeUser.getRole());
                },
                () -> assertEquals(menteeUpdateRequest.getSubjects(), mentee.getSubjects())
        );
    }

    @DisplayName("멘티 탈퇴 <> 사용자 탈퇴")
    @Test
    void quiting_mentee_not_equals_to_quiting_user() {

        // Given
        // When
        menteeService.deleteMentee(menteeUser);

        // Then
        User _menteeUser = userRepository.findByUsername(menteeUser.getUsername()).orElse(null);
        assertAll(
                () -> assertNotNull(_menteeUser),
                () -> assertEquals(0, chatroomRepository.findByMenteeOrderByIdDesc(mentee).size()),
                () -> assertEquals(0, pickRepository.findByMentee(mentee).size()),
                () -> assertEquals(0, enrollmentRepository.findByMentee(mentee).size()),
                () -> assertNull(menteeRepository.findByUser(_menteeUser)),
                // not deleted
                () -> {
                    assert _menteeUser != null;
                    assertFalse(_menteeUser.isDeleted());
                },
                () -> {
                    assert _menteeUser != null;
                    assertNull(_menteeUser.getDeletedAt());
                }
        );
    }
}