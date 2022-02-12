package com.project.mentoridge.modules.review.controller.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenteeReviewCreateRequest {

    @Min(0) @Max(5)
    @NotNull
    private Integer score;

    @NotBlank
    private String content;

    @Builder(access = AccessLevel.PRIVATE)
    private MenteeReviewCreateRequest(@Min(0) @Max(5) @NotNull Integer score, @NotBlank String content) {
        this.score = score;
        this.content = content;
    }

    public static MenteeReviewCreateRequest of(Integer score, String content) {
        return MenteeReviewCreateRequest.builder()
                .score(score)
                .content(content)
                .build();
    }
}
