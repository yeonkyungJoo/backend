package com.project.mentoridge.modules.purchase.repository;

import com.project.mentoridge.modules.account.vo.Mentor;
import com.project.mentoridge.modules.account.vo.QMentee;
import com.project.mentoridge.modules.account.vo.QUser;
import com.project.mentoridge.modules.chat.vo.QChatroom;
import com.project.mentoridge.modules.lecture.vo.Lecture;
import com.project.mentoridge.modules.lecture.vo.LecturePrice;
import com.project.mentoridge.modules.lecture.vo.QLecture;
import com.project.mentoridge.modules.lecture.vo.QLecturePrice;
import com.project.mentoridge.modules.purchase.controller.response.CancellationResponse;
import com.project.mentoridge.modules.purchase.vo.Cancellation;
import com.project.mentoridge.modules.purchase.vo.QCancellation;
import com.project.mentoridge.modules.purchase.vo.QEnrollment;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Repository
public class CancellationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QCancellation cancellation = QCancellation.cancellation;
    private final QEnrollment enrollment = QEnrollment.enrollment;
    private final QChatroom chatroom = QChatroom.chatroom;
    private final QLecture lecture = QLecture.lecture;
    private final QLecturePrice lecturePrice = QLecturePrice.lecturePrice;
    private final QMentee mentee = QMentee.mentee;
    private final QUser user = QUser.user;

    public Page<CancellationResponse> findCancellationsOfMentor(Mentor mentor, Pageable pageable) {

        /*
        SELECT * FROM cancellation c
        INNER JOIN enrollment e ON c.enrollment_id = e.enrollment_id
        INNER JOIN lecture l ON e.lecture_id = l.lecture_id
        INNER JOIN lecture_price lp ON e.lecture_price_id = lp.lecture_price_id
        INNER JOIN mentee t ON e.mentee_id = t.mentee_id
        INNER JOIN user u ON t.user_id = u.user_id
        */
        QueryResults<Tuple> tuples = jpaQueryFactory.select(cancellation, lecture, lecturePrice, mentee.id, user.name, enrollment.id)
                .from(cancellation)
                .innerJoin(cancellation.enrollment, enrollment)
                .innerJoin(enrollment.lecture, lecture)
                .innerJoin(enrollment.lecturePrice, lecturePrice)
                .innerJoin(enrollment.mentee, mentee)
                .innerJoin(mentee.user, user)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .where(lecture.mentor.eq(mentor))
                .fetchResults();

        // TODO - CHECK
        List<Long> enrollmentIds = tuples.getResults().stream().map(tuple -> tuple.get(5, Long.class)).collect(Collectors.toList());
        QueryResults<Tuple> ids = jpaQueryFactory.select(chatroom.id, enrollment.id)
                .from(chatroom)
                .innerJoin(chatroom.enrollment, enrollment)
                .where(enrollment.id.in(enrollmentIds))
                .fetchResults();
        Map<Long, Long> map = ids.getResults().stream()
                .collect(Collectors.toMap(tuple -> tuple.get(1, Long.class), tuple -> tuple.get(0, Long.class)));

        List<CancellationResponse> cancellationResponses = tuples.getResults().stream()
                .map(tuple -> CancellationResponse.builder()
                        .cancellation(tuple.get(0, Cancellation.class))
                        .lecture(tuple.get(1, Lecture.class))
                        .lecturePrice(tuple.get(2, LecturePrice.class))
                        .menteeId(tuple.get(3, Long.class))
                        .menteeName(tuple.get(4, String.class))
                        .chatroomId(map.get(tuple.get(5, Long.class)))
                        .build()).collect(Collectors.toList());
        return new PageImpl<>(cancellationResponses, pageable, tuples.getTotal());
    }

}
