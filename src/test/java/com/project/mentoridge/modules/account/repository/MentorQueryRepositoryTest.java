package com.project.mentoridge.modules.account.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.project.mentoridge.modules.account.controller.response.MenteeLectureResponse;
import com.project.mentoridge.modules.account.controller.response.MenteeSimpleResponse;
import com.project.mentoridge.modules.account.vo.Mentor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class MentorQueryRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MentorRepository mentorRepository;

    @Autowired
    MenteeRepository menteeRepository;

    private MentorQueryRepository mentorQueryRepository;

    @BeforeEach
    void setup() {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        mentorQueryRepository = new MentorQueryRepository(jpaQueryFactory);

        assertNotNull(em);
        assertNotNull(jpaQueryFactory);
        assertNotNull(mentorQueryRepository);
    }

    @Test
    void findMenteesOfMentor() {

        // given
        Mentor mentor = mentorRepository.findAll().stream().findFirst()
                .orElseThrow(RuntimeException::new);
        // when
        // then
        Page<MenteeSimpleResponse> result = mentorQueryRepository.findMenteesOfMentor(mentor, false, Pageable.ofSize(20));
        result.forEach(System.out::println);
    }

    @Test
    void findMenteeLecturesOfMentor() {

        // given
        Mentor mentor = mentorRepository.findAll().stream().findFirst()
                .orElseThrow(RuntimeException::new);
        Long menteeId = menteeRepository.findAll().stream().findFirst()
                .orElseThrow(RuntimeException::new).getId();
        // when
        // then
        Page<MenteeLectureResponse> result = mentorQueryRepository.findMenteeLecturesOfMentor(mentor, false, menteeId, Pageable.ofSize(20));
        result.forEach(System.out::println);
    }
}