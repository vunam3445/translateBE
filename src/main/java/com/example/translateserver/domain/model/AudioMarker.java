package com.example.translateserver.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioMarker {
    private Long id;
    private Long chapterId;
    private int sentenceOrder;
    private int startTimeMs;
    private int endTimeMs;
}
