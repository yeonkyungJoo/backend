package com.project.mentoridge.modules.account.service;

import com.project.mentoridge.config.exception.AlreadyExistException;
import com.project.mentoridge.config.security.jwt.JwtTokenManager;
import com.project.mentoridge.configuration.auth.WithAccount;
import com.project.mentoridge.modules.account.controller.request.LoginRequest;
import com.project.mentoridge.modules.account.enums.RoleType;
import com.project.mentoridge.modules.account.repository.MenteeRepository;
import com.project.mentoridge.modules.account.repository.UserRepository;
import com.project.mentoridge.modules.account.vo.Mentee;
import com.project.mentoridge.modules.account.vo.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import static com.project.mentoridge.config.init.TestDataBuilder.getLoginRequestWithUsernameAndPassword;
import static com.project.mentoridge.configuration.AbstractTest.loginRequest;
import static com.project.mentoridge.configuration.AbstractTest.signUpRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class LoginServiceIntegrationTest {

    private static final String NAME = "user";
    private static final String NICKNAME = NAME;
    private static final String USERNAME = "user@email.com";

    @Autowired
    JwtTokenManager jwtTokenManager;

    @Autowired
    LoginService loginService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MenteeRepository menteeRepository;

    @WithAccount(NAME)
    @Test
    void check_username_existed() {

        // Given
        User user = userRepository.findByUsername("user").orElse(null);
        assertNotNull(user);

        // When
        // Then
        boolean result = loginService.checkUsernameDuplication("user@email.com");
        assertTrue(result);
    }

    @Test
    void check_username_not_existed() {

        // Given
        // When
        // Then
        boolean result = loginService.checkUsernameDuplication("new@email.com");
        assertFalse(result);
    }

    @WithAccount(NAME)
    @Test
    void check_nickname_existed() {

        // Given
        // When
        // Then
        boolean result = loginService.checkNicknameDuplication("user");
        assertTrue(result);
    }

    @Test
    void check_nickname_not_existed() {

        // Given
        // When
        // Then
        boolean result = loginService.checkNicknameDuplication("new");
        assertFalse(result);
    }

    @Test
    void 회원가입() {

        // Given
        // When
        loginService.signUp(signUpRequest);

        // Then
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        assertNull(user);

        User unverifiedUser = userRepository.findAllByUsername(USERNAME);
        assertAll(
                () -> assertNotNull(unverifiedUser),
                () -> assertFalse(unverifiedUser.isEmailVerified()),
                () -> assertEquals(RoleType.MENTEE, unverifiedUser.getRole()),
                () -> assertEquals(signUpRequest.getZone(), unverifiedUser.getZone().toString()),
                () -> assertEquals(signUpRequest.getPhoneNumber(), unverifiedUser.getPhoneNumber())
        );

        Mentee mentee = menteeRepository.findByUser(user);
        assertNull(mentee);
    }

    @DisplayName("회원가입 - AlreadyExistException 발생")
    @Test
    void signUpWithExistingUsername() {

        // Given
        loginService.signUp(signUpRequest);
        assertNotNull(userRepository.findAllByUsername(USERNAME));

        // When
        assertThrows(AlreadyExistException.class, () -> {
            loginService.signUp(signUpRequest);
        });
    }

    @Test
    void verifyEmail() {

        // Given
        User user = loginService.signUp(signUpRequest);
        assertFalse(user.isEmailVerified());
        assertFalse(userRepository.findByUsername(USERNAME).isPresent());
        assertNotNull(userRepository.findAllByUsername(USERNAME));

        // When
        Mentee mentee = loginService.verifyEmail(user.getUsername(), user.getEmailVerifyToken());

        // Then
        user = userRepository.findByUsername(USERNAME).orElse(null);
        assertNotNull(user);
        assertTrue(user.isEmailVerified());

        assertNotNull(mentee);
        assertEquals(user, mentee.getUser());
    }

    @Test
    void verifyEmail_with_wrongToken() {

        // Given
        User user = loginService.signUp(signUpRequest);
        assertFalse(user.isEmailVerified());
        assertFalse(userRepository.findByUsername(USERNAME).isPresent());
        assertNotNull(userRepository.findAllByUsername(USERNAME));

        // When
        // Then
        assertThrows(RuntimeException.class,
                () -> loginService.verifyEmail(user.getUsername(), user.getEmailVerifyToken()));
    }

    @Test
    void login() {

        // Given
        User user = loginService.signUp(signUpRequest);
        loginService.verifyEmail(user.getUsername(), user.getEmailVerifyToken());

        // When
        JwtTokenManager.JwtResponse result = loginService.login(loginRequest);

        // Then
        assertFalse(result.getAccessToken().isEmpty());
        assertFalse(result.getRefreshToken().isEmpty());
        String accessToken = result.getAccessToken();
        assertEquals(USERNAME, jwtTokenManager.getClaim(accessToken, "username"));
    }

    @DisplayName("이메일 미인증 사용자")
    @Test
    void login_by_unverifiedUser() {

        // Given
        loginService.signUp(signUpRequest);

        // When
        // Then
        assertThrows(BadCredentialsException.class, () -> loginService.login(loginRequest));
    }

    @Test
    void login_with_wrongPassword() {

        // Given
        User user = loginService.signUp(signUpRequest);
        loginService.verifyEmail(user.getUsername(), user.getEmailVerifyToken());

        // When
        // Then
        LoginRequest loginRequest = getLoginRequestWithUsernameAndPassword(USERNAME, "password_");
        assertThrows(BadCredentialsException.class, () -> loginService.login(loginRequest));
    }

    @Test
    void find_password() {

        // Given
        User user = loginService.signUp(signUpRequest);
        loginService.verifyEmail(user.getUsername(), user.getEmailVerifyToken());
        String password = user.getPassword();

        // When
        loginService.findPassword(USERNAME);
        // Then
        assertNotEquals(password, user.getPassword());
    }

    @Test
    void change_type() {

        // Given
        User user = loginService.signUp(signUpRequest);
        loginService.verifyEmail(user.getUsername(), user.getEmailVerifyToken());

        // When
        // 1. 멘토로 변경
        String mentorToken = loginService.changeType(user.getUsername(), RoleType.MENTOR.getType()).getAccessToken();
        // 2. 멘티로 변경
        String menteeToken = loginService.changeType(user.getUsername(), RoleType.MENTEE.getType()).getAccessToken();

        // Then
        assertThat(mentorToken).isNotEqualTo(menteeToken);
        assertThat(jwtTokenManager.getClaim(mentorToken, "role")).isEqualTo(RoleType.MENTOR.getType());
        assertThat(jwtTokenManager.getClaim(menteeToken, "role")).isEqualTo(RoleType.MENTEE.getType());
    }
}