package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.AnalyzedSentence;
import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.domain.repository.AnalyzedSentenceRepository;
import com.example.translateserver.domain.repository.StoryGlossaryRepository;
import com.example.translateserver.infrastructure.web.dto.GlossaryMismatchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetGlossaryMismatchesUseCase {

    private final AnalyzedSentenceRepository sentenceRepository;
    private final StoryGlossaryRepository glossaryRepository;

    public List<GlossaryMismatchResponse> execute(Long storyId, Long chapterId) {
        List<StoryGlossary> glossaries = glossaryRepository.findByStoryId(storyId);
        List<AnalyzedSentence> sentences = sentenceRepository.findByChapterId(chapterId);
        List<GlossaryMismatchResponse> mismatches = new ArrayList<>();

        for (AnalyzedSentence sentence : sentences) {
            List<GlossaryMismatchResponse.MismatchedTerm> mismatchedTerms = new ArrayList<>();
            for (StoryGlossary g : glossaries) {
                String viTerm = g.getViTerm();
                String enTerm = g.getEnTerm();

                // Câu gốc tiếng Việt chứa từ khóa glossary, nhưng bản dịch tiếng Anh chưa chứa từ khóa tiếng Việt có dấu
                if (sentence.getViText() != null && sentence.getViText().contains(viTerm)) {
                    if (sentence.getEnText() == null || !sentence.getEnText().contains(viTerm)) {
                        mismatchedTerms.add(new GlossaryMismatchResponse.MismatchedTerm(viTerm, enTerm, enTerm));
                    }
                }
            }

            if (!mismatchedTerms.isEmpty()) {
                mismatches.add(GlossaryMismatchResponse.builder()
                        .sentenceId(sentence.getId())
                        .sentenceOrder(sentence.getSentenceOrder())
                        .viText(sentence.getViText())
                        .enText(sentence.getEnText())
                        .mismatchedTerms(mismatchedTerms)
                        .build());
            }
        }

        return mismatches;
    }
}
