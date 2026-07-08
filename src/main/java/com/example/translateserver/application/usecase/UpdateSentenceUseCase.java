package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.AnalyzedSentence;
import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.repository.AnalyzedSentenceRepository;
import com.example.translateserver.domain.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateSentenceUseCase {

    private final AnalyzedSentenceRepository sentenceRepository;
    private final ChapterRepository chapterRepository;

    public void execute(Long chapterId, Long sentenceId, String newEnText) {
        List<AnalyzedSentence> sentences = sentenceRepository.findByChapterId(chapterId);
        AnalyzedSentence sentence = sentences.stream()
                .filter(s -> s.getId().equals(sentenceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu với ID: " + sentenceId + " trong chương " + chapterId));

        sentence.setEnText(newEnText);
        sentenceRepository.save(sentence);

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương với ID: " + chapterId));

        // Nối lại toàn bộ các câu trong chương sau khi cập nhật
        List<AnalyzedSentence> updatedSentences = sentenceRepository.findByChapterId(chapterId);
        updatedSentences.sort((a, b) -> Integer.compare(a.getSentenceOrder(), b.getSentenceOrder()));
        
        String fullEnText = updatedSentences.stream()
                .map(AnalyzedSentence::getEnText)
                .collect(Collectors.joining("\n\n"));

        chapter.setTranslatedContent(fullEnText);
        chapterRepository.save(chapter);
    }
}
