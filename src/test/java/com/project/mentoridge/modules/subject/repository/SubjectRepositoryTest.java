package com.project.mentoridge.modules.subject.repository;

import com.project.mentoridge.modules.lecture.enums.LearningKindType;
import com.project.mentoridge.modules.subject.vo.Subject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

@Disabled
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class SubjectRepositoryTest {

    @Autowired
    SubjectRepository subjectRepository;

    @Test
    void getLearningKinds() {

        // given
        // when
        // then
        List<LearningKindType> learningKinds = subjectRepository.findLearningKinds();
        learningKinds.forEach(System.out::println);
    }

    @Test
    void findAllByLearningKindId() {

        // given
        LearningKindType learningKind = subjectRepository.findLearningKinds().stream().findFirst()
                .orElseThrow(RuntimeException::new);
        // when
        // then
        List<Subject> subjects = subjectRepository.findAllByLearningKind(learningKind);
        subjects.forEach(System.out::println);
    }

}