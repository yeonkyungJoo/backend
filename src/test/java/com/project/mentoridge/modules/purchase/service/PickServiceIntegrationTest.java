package com.project.mentoridge.modules.purchase.service;

import com.project.mentoridge.configuration.auth.WithAccount;
import com.project.mentoridge.modules.account.repository.MenteeRepository;
import com.project.mentoridge.modules.account.repository.UserRepository;
import com.project.mentoridge.modules.account.service.LoginService;
import com.project.mentoridge.modules.account.service.MentorService;
import com.project.mentoridge.modules.account.vo.Mentee;
import com.project.mentoridge.modules.account.vo.Mentor;
import com.project.mentoridge.modules.account.vo.User;
import com.project.mentoridge.modules.lecture.controller.request.LectureCreateRequest;
import com.project.mentoridge.modules.lecture.enums.DifficultyType;
import com.project.mentoridge.modules.lecture.enums.LearningKindType;
import com.project.mentoridge.modules.lecture.enums.SystemType;
import com.project.mentoridge.modules.lecture.repository.LecturePriceRepository;
import com.project.mentoridge.modules.lecture.service.LectureService;
import com.project.mentoridge.modules.lecture.vo.Lecture;
import com.project.mentoridge.modules.lecture.vo.LecturePrice;
import com.project.mentoridge.modules.log.component.LectureLogService;
import com.project.mentoridge.modules.log.component.UserLogService;
import com.project.mentoridge.modules.purchase.controller.response.PickWithSimpleLectureResponse;
import com.project.mentoridge.modules.purchase.repository.PickRepository;
import com.project.mentoridge.modules.purchase.vo.Pick;
import com.project.mentoridge.modules.subject.repository.SubjectRepository;
import com.project.mentoridge.modules.subject.vo.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;

import static com.project.mentoridge.config.init.TestDataBuilder.getSignUpRequestWithNameAndNickname;
import static com.project.mentoridge.configuration.AbstractTest.lectureCreateRequest;
import static com.project.mentoridge.configuration.AbstractTest.mentorSignUpRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class PickServiceIntegrationTest {

    private static final String NAME = "user";
    private static final String USERNAME = "user@email.com";

    @Autowired
    PickService pickService;
    @Autowired
    PickRepository pickRepository;

    @Autowired
    LoginService loginService;
    @Autowired
    UserLogService userLogService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MenteeRepository menteeRepository;
    @Autowired
    MentorService mentorService;
    @Autowired
    LectureService lectureService;
    @Autowired
    LectureLogService lectureLogService;
    @Autowired
    LecturePriceRepository lecturePriceRepository;
    @Autowired
    SubjectRepository subjectRepository;

    private Lecture lecture1;
    private Lecture lecture2;

    @BeforeEach
    void init() {

        // subject
        if (subjectRepository.count() == 0) {
            subjectRepository.save(Subject.builder()
                    .subjectId(1L)
                    .learningKind(LearningKindType.IT)
                    .krSubject("백엔드")
                    .build());
            subjectRepository.save(Subject.builder()
                    .subjectId(2L)
                    .learningKind(LearningKindType.IT)
                    .krSubject("프론트엔드")
                    .build());
        }

        User mentorUser = loginService.signUp(getSignUpRequestWithNameAndNickname("mentor", "mentor"));
        // loginService.verifyEmail(mentorUser.getUsername(), mentorUser.getEmailVerifyToken());
        mentorUser.verifyEmail(userLogService);
        menteeRepository.save(Mentee.builder()
                .user(mentorUser)
                .build());
        Mentor mentor = mentorService.createMentor(mentorUser, mentorSignUpRequest);

        lecture1 = lectureService.createLecture(mentorUser, lectureCreateRequest);
        lecture1.approve(lectureLogService);

        lecture2 = lectureService.createLecture(mentorUser, LectureCreateRequest.builder()
                .title("제목2")
                .subTitle("소제목2")
                .introduce("소개2")
                .content("<p>본문2</p>")
                .difficulty(DifficultyType.ADVANCED)
                .systems(Arrays.asList(SystemType.ONLINE, SystemType.OFFLINE))
                .lecturePrices(Arrays.asList(LectureCreateRequest.LecturePriceCreateRequest.builder()
                        .isGroup(false)
                        .pricePerHour(2000L)
                        .timePerLecture(5)
                        .numberOfLectures(5)
                        .totalPrice(2000L * 5 * 5)
                        .build()))
                .lectureSubjects(Arrays.asList(LectureCreateRequest.LectureSubjectCreateRequest.builder()
                        .subjectId(2L)
                        .build()))
                .thumbnail("https://mentoridge.s3.ap-northeast-2.amazonaws.com/2bb34d85-dfa5-4b0e-bc1d-094537af475c")
                .build());
        lecture2.approve(lectureLogService);
    }

    @WithAccount(NAME)
    @Test
    void get_paged_PickWithSimpleLectureResponses() {

        // Given
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        Mentee mentee = menteeRepository.findByUser(user);
        assertNotNull(user);

        LecturePrice lecturePrice1 = lecturePriceRepository.findByLecture(lecture1).get(0);
        LecturePrice lecturePrice2 = lecturePriceRepository.findByLecture(lecture2).get(0);
        Pick pick1 = Pick.buildPick(mentee, lecture1, lecturePrice1);
        Pick pick2 = Pick.buildPick(mentee, lecture2, lecturePrice2);
        pickRepository.saveAll(Arrays.asList(pick1, pick2));

        // When
        Page<PickWithSimpleLectureResponse> picks = pickService.getPickWithSimpleLectureResponses(user, 1);

        // Then
        assertThat(picks.getTotalElements()).isEqualTo(2L);
        for (PickWithSimpleLectureResponse pick : picks) {

            if (Objects.equals(pick.getPickId(), pick1.getId())) {

                assertAll(
                        () -> assertThat(pick.getPickId()).isEqualTo(pick1.getId()),
                        () -> assertThat(pick.getLecture().getId()).isEqualTo(lecture1.getId()),
                        () -> assertThat(pick.getLecture().getTitle()).isEqualTo(lecture1.getTitle()),
                        () -> assertThat(pick.getLecture().getSubTitle()).isEqualTo(lecture1.getSubTitle()),
                        () -> assertThat(pick.getLecture().getIntroduce()).isEqualTo(lecture1.getIntroduce()),
                        () -> assertThat(pick.getLecture().getDifficulty()).isEqualTo(lecture1.getDifficulty()),

                        () -> assertThat(pick.getLecture().getSystems().size()).isEqualTo(lecture1.getSystems().size()),

                        () -> assertThat(pick.getLecture().getLecturePrice().getLecturePriceId()).isEqualTo(lecturePrice1.getId()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getIsGroup()).isEqualTo(lecturePrice1.getIsGroup()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getNumberOfMembers()).isEqualTo(lecturePrice1.getNumberOfMembers()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getPricePerHour()).isEqualTo(lecturePrice1.getPricePerHour()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getTimePerLecture()).isEqualTo(lecturePrice1.getTimePerLecture()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getNumberOfLectures()).isEqualTo(lecturePrice1.getNumberOfLectures()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getTotalPrice()).isEqualTo(lecturePrice1.getTotalPrice()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getIsGroupStr()).isEqualTo(lecturePrice1.getIsGroup() ? "그룹강의" : "1:1 개인강의"),
                        () -> assertThat(pick.getLecture().getLecturePrice().getContent()).isEqualTo(String.format("시간당 %d원 x 1회 %d시간 x 총 %d회 수업 진행", lecturePrice1.getPricePerHour(), lecturePrice1.getTimePerLecture(), lecturePrice1.getNumberOfLectures())),
                        () -> assertThat(pick.getLecture().getLecturePrice().getClosed()).isEqualTo(lecturePrice1.isClosed()),

                        () -> assertThat(pick.getLecture().getLectureSubjects().size()).isEqualTo(lecture1.getLectureSubjects().size()),

                        () -> assertThat(pick.getLecture().getThumbnail()).isEqualTo(lecture1.getThumbnail()),
                        () -> assertThat(pick.getLecture().getMentorNickname()).isEqualTo(lecture1.getMentor().getUser().getNickname()),
                        () -> assertThat(pick.getLecture().getScoreAverage()).isEqualTo(0),
                        () -> assertThat(pick.getLecture().getPickCount()).isEqualTo(1L)
                );

            } else if (Objects.equals(pick.getPickId(), pick2.getId())) {

                assertAll(
                        () -> assertThat(pick.getPickId()).isEqualTo(pick2.getId()),
                        () -> assertThat(pick.getLecture().getId()).isEqualTo(lecture2.getId()),
                        () -> assertThat(pick.getLecture().getTitle()).isEqualTo(lecture2.getTitle()),
                        () -> assertThat(pick.getLecture().getSubTitle()).isEqualTo(lecture2.getSubTitle()),
                        () -> assertThat(pick.getLecture().getIntroduce()).isEqualTo(lecture2.getIntroduce()),
                        () -> assertThat(pick.getLecture().getDifficulty()).isEqualTo(lecture2.getDifficulty()),

                        () -> assertThat(pick.getLecture().getSystems().size()).isEqualTo(lecture2.getSystems().size()),

                        () -> assertThat(pick.getLecture().getLecturePrice().getLecturePriceId()).isEqualTo(lecturePrice2.getId()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getIsGroup()).isEqualTo(lecturePrice2.getIsGroup()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getNumberOfMembers()).isEqualTo(lecturePrice2.getNumberOfMembers()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getPricePerHour()).isEqualTo(lecturePrice2.getPricePerHour()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getTimePerLecture()).isEqualTo(lecturePrice2.getTimePerLecture()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getNumberOfLectures()).isEqualTo(lecturePrice2.getNumberOfLectures()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getTotalPrice()).isEqualTo(lecturePrice2.getTotalPrice()),
                        () -> assertThat(pick.getLecture().getLecturePrice().getIsGroupStr()).isEqualTo(lecturePrice2.getIsGroup() ? "그룹강의" : "1:1 개인강의"),
                        () -> assertThat(pick.getLecture().getLecturePrice().getContent()).isEqualTo(String.format("시간당 %d원 x 1회 %d시간 x 총 %d회 수업 진행", lecturePrice2.getPricePerHour(), lecturePrice2.getTimePerLecture(), lecturePrice2.getNumberOfLectures())),
                        () -> assertThat(pick.getLecture().getLecturePrice().getClosed()).isEqualTo(lecturePrice2.isClosed()),

                        () -> assertThat(pick.getLecture().getLectureSubjects().size()).isEqualTo(lecture2.getLectureSubjects().size()),

                        () -> assertThat(pick.getLecture().getThumbnail()).isEqualTo(lecture2.getThumbnail()),
                        () -> assertThat(pick.getLecture().getMentorNickname()).isEqualTo(lecture2.getMentor().getUser().getNickname()),
                        () -> assertThat(pick.getLecture().getScoreAverage()).isEqualTo(0),
                        () -> assertThat(pick.getLecture().getPickCount()).isEqualTo(1L)
                );
            } else {
                fail();
            }
        }

    }

    @WithAccount(NAME)
    @Test
    void createPick() {

        // Given
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        Mentee mentee = menteeRepository.findByUser(user);
        assertNotNull(user);

        // When
        LecturePrice lecturePrice1 = lecturePriceRepository.findByLecture(lecture1).get(0);
        Long pickId = pickService.createPick(user, lecture1.getId(), lecturePrice1.getId());

        // Then
        Pick pick = pickRepository.findById(pickId).orElse(null);
        assertAll(
                () -> assertNotNull(pick),
                () -> assertEquals(mentee, pick.getMentee()),
                () -> assertEquals(lecture1, pick.getLecture()),
                () -> assertEquals(lecturePrice1, pick.getLecturePrice())
        );
    }

    @WithAccount(NAME)
    @Test
    void cancelPick() {

        // Given
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        Mentee mentee = menteeRepository.findByUser(user);
        assertNotNull(user);

        LecturePrice lecturePrice1 = lecturePriceRepository.findByLecture(lecture1).get(0);
        Long pickId = pickService.createPick(user, lecture1.getId(), lecturePrice1.getId());

        // When
        Long result = pickService.createPick(user, lecture1.getId(), lecturePrice1.getId());

        // Then
        assertNull(result);
        assertFalse(pickRepository.findById(pickId).isPresent());
        assertTrue(pickRepository.findByMentee(mentee).isEmpty());
    }

/*
    @WithAccount(NAME)
    @Test
    void deletePick() {

        // Given
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        Mentee mentee = menteeRepository.findByUser(user);
        assertNotNull(user);

        LecturePrice lecturePrice1 = lecturePriceRepository.findByLecture(lecture).get(0);
        Long pickId = pickService.createPick(user, lecture.getId(), lecturePrice1.getId()).getId();

        // When
        pickService.deletePick(user, pickId);

        // Then
        Pick pick = pickRepository.findById(pickId).orElse(null);
        assertNull(pick);
        assertTrue(pickRepository.findByMentee(mentee).isEmpty());
    }*/

    @WithAccount(NAME)
    @Test
    void deleteAllPicks() {

        // Given
        User user = userRepository.findByUsername(USERNAME).orElse(null);
        Mentee mentee = menteeRepository.findByUser(user);
        assertNotNull(user);

        LecturePrice lecturePrice1 = lecturePriceRepository.findByLecture(lecture1).get(0);
        LecturePrice lecturePrice2 = lecturePriceRepository.findByLecture(lecture2).get(0);
        Long pick1Id = pickService.createPick(user, lecture1.getId(), lecturePrice1.getId());
        Long pick2Id = pickService.createPick(user, lecture2.getId(), lecturePrice2.getId());
        assertEquals(2, pickRepository.findByMentee(mentee).size());

        // When
        pickService.deleteAllPicks(user);

        // Then
        assertTrue(pickRepository.findByMentee(mentee).isEmpty());
        assertFalse(pickRepository.findById(pick1Id).isPresent());
        assertFalse(pickRepository.findById(pick2Id).isPresent());
    }
}