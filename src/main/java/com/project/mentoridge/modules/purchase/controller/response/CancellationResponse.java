package com.project.mentoridge.modules.purchase.controller.response;

import com.project.mentoridge.modules.lecture.vo.Lecture;
import com.project.mentoridge.modules.lecture.vo.LecturePrice;
import com.project.mentoridge.modules.purchase.vo.Cancellation;
import com.project.mentoridge.utils.LocalDateTimeUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

@Data
public class CancellationResponse {

    private Long cancellationId;
    private String reason;
    private boolean approved;
    // Java 8 date/time type `java.time.LocalDateTime` not supported by default
    // private LocalDateTime createdAt;
    private String createdAt;
    private EnrolledLectureResponse lecture;

    private Long menteeId;
    private String menteeName;

    private Long chatroomId;

    @Builder(access = AccessLevel.PUBLIC)
    private CancellationResponse(Cancellation cancellation, Lecture lecture, LecturePrice lecturePrice, Long menteeId, String menteeName, Long chatroomId) {
        this.cancellationId = cancellation.getId();
        this.reason = cancellation.getReason();
        this.approved = cancellation.isApproved();
        this.createdAt = LocalDateTimeUtil.getDateTimeToString(cancellation.getCreatedAt());
        this.lecture = new EnrolledLectureResponse(lecture, lecturePrice);
        this.menteeId = menteeId;
        this.menteeName = menteeName;
        this.chatroomId = chatroomId;
    }
}
