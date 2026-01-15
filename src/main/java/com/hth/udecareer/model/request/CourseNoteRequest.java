package com.hth.udecareer.model.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseNoteRequest {
    private String content;
}