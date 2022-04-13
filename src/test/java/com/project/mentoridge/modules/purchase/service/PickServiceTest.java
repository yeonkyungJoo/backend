package com.project.mentoridge.modules.purchase.service;

import com.project.mentoridge.modules.account.repository.MenteeRepository;
import com.project.mentoridge.modules.account.vo.Mentee;
import com.project.mentoridge.modules.account.vo.User;
import com.project.mentoridge.modules.lecture.repository.LecturePriceRepository;
import com.project.mentoridge.modules.lecture.repository.LectureRepository;
import com.project.mentoridge.modules.lecture.vo.Lecture;
import com.project.mentoridge.modules.lecture.vo.LecturePrice;
import com.project.mentoridge.modules.log.component.PickLogService;
import com.project.mentoridge.modules.purchase.repository.PickRepository;
import com.project.mentoridge.modules.purchase.vo.Pick;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.project.mentoridge.modules.purchase.vo.Pick.buildPick;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickServiceTest {

    @InjectMocks
    PickServiceImpl pickService;
    @Mock
    PickRepository pickRepository;

    @Mock
    MenteeRepository menteeRepository;
    @Mock
    LectureRepository lectureRepository;
    @Mock
    LecturePriceRepository lecturePriceRepository;

    @Mock
    PickLogService pickLogService;

    @Test
    void createPick() {
        // user(mentee), lectureId

        // given
        Mentee mentee = Mockito.mock(Mentee.class);
        when(menteeRepository.findByUser(any(User.class))).thenReturn(mentee);

        Lecture lecture = Mockito.mock(Lecture.class);
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));

        LecturePrice lecturePrice = Mockito.mock(LecturePrice.class);
        when(lecturePriceRepository.findByLectureAndId(lecture, 1L)).thenReturn(Optional.of(lecturePrice));

        // when
        User user = Mockito.mock(User.class);
        pickService.createPick(user, 1L, 1L);

        // then
        // pick 생성
        verify(pickRepository).save(buildPick(mentee, lecture, lecturePrice));
    }

    @Test
    void deletePick() {
        // user(mentee), pickId

        // given
        Mentee mentee = Mockito.mock(Mentee.class);
        when(menteeRepository.findByUser(any(User.class))).thenReturn(mentee);

        Pick pick = Mockito.mock(Pick.class);
        when(pickRepository.findByMenteeAndId(any(Mentee.class), anyLong())).thenReturn(Optional.of(pick));

        // when
        User user = Mockito.mock(User.class);
        pickService.deletePick(user, 1L);

        // then
        verify(pick).delete();
        verify(pickRepository).delete(pick);
    }

    @Test
    void deleteAllPicks() {
        // user(mentee)

        // given
        Mentee mentee = Mockito.mock(Mentee.class);
        when(menteeRepository.findByUser(any(User.class))).thenReturn(mentee);

        // when
        User user = Mockito.mock(User.class);
        pickService.deleteAllPicks(user);

        // then
        verify(pickRepository).deleteByMentee(mentee);
    }
}