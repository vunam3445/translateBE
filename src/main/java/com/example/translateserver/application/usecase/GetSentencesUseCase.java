package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.AnalyzedSentence;
import com.example.translateserver.domain.repository.AnalyzedSentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSentencesUseCase {

    private final AnalyzedSentenceRepository sentenceRepository;

    public List<AnalyzedSentence> execute(Long chapterId) {
        return sentenceRepository.findByChapterId(chapterId);
    }
}
