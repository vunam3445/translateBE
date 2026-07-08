package com.example.translateserver.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlossaryMismatchResponse {
    private Long sentenceId;
    private int sentenceOrder;
    private String viText;
    private String enText;
    private List<MismatchedTerm> mismatchedTerms;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MismatchedTerm {
        private String viTerm;
        private String enTerm;
        private String suggestedTarget;
    }
}
