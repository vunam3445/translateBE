package com.example.translateserver.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentenceResponse {
    private Long id;
    private Long chapterId;
    private int sentenceOrder;
    private String viText;
    private String enText;
    private String tense;
    private String wordsJsonMeta;
}
