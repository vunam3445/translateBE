package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.AnalyzedSentence;
import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.domain.repository.AnalyzedSentenceRepository;
import com.example.translateserver.domain.repository.ChapterRepository;
import com.example.translateserver.domain.util.PinyinConverter;
import com.example.translateserver.domain.util.StringSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropagateGlossaryUseCase {

    private final ChapterRepository chapterRepository;
    private final AnalyzedSentenceRepository sentenceRepository;

    public void executeAsync(Long storyId, StoryGlossary glossary) {
        CompletableFuture.runAsync(() -> {
            try {
                execute(storyId, glossary);
            } catch (Exception e) {
                log.error("Lỗi khi đồng bộ glossary ngầm cho story {}", storyId, e);
            }
        });
    }

    public void execute(Long storyId, StoryGlossary glossary) {
        log.info("Bắt đầu đồng bộ glossary ngầm cho story: {}, glossary: {} -> {}", storyId, glossary.getEnTerm(), glossary.getViTerm());
        String viTerm = glossary.getViTerm();
        String enTerm = glossary.getEnTerm();

        Set<String> searchCandidates = new HashSet<>();
        searchCandidates.add(enTerm);
        searchCandidates.add(StringSanitizer.stripAccents(viTerm));
        searchCandidates.add(PinyinConverter.convertToPinyin(viTerm));

        List<String> cleanCandidates = searchCandidates.stream()
                .filter(c -> c != null && !c.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());

        List<Chapter> chapters = chapterRepository.findByStoryId(storyId);
        for (Chapter chapter : chapters) {
            List<AnalyzedSentence> sentences = sentenceRepository.findByChapterId(chapter.getId());
            boolean chapterUpdated = false;

            for (AnalyzedSentence sentence : sentences) {
                if (sentence.getViText() != null) {
                    String cleanViText = StringSanitizer.stripAccents(sentence.getViText()).toLowerCase();
                    String cleanViTerm = StringSanitizer.stripAccents(viTerm).toLowerCase();
                    String cleanEnTerm = enTerm.toLowerCase();
                    
                    if (cleanViText.contains(cleanViTerm) || cleanViText.contains(cleanEnTerm)) {
                        String enText = sentence.getEnText();
                        if (enText == null) continue;
 
                        boolean sentenceUpdated = false;
                        for (String candidate : cleanCandidates) {
                            String regex = "(?i)\\b" + Pattern.quote(candidate) + "\\b";
                            Pattern pattern = Pattern.compile(regex);
                            if (pattern.matcher(enText).find()) {
                                enText = enText.replaceAll(regex, viTerm);
                                sentenceUpdated = true;
                            }
                        }
 
                        if (sentenceUpdated) {
                            sentence.setEnText(enText);
                            sentenceRepository.save(sentence);
                            chapterUpdated = true;
                        }
                    }
                }
            }

            if (chapterUpdated) {
                List<AnalyzedSentence> sortedSentences = sentenceRepository.findByChapterId(chapter.getId());
                sortedSentences.sort((a, b) -> Integer.compare(a.getSentenceOrder(), b.getSentenceOrder()));
                String fullEnText = sortedSentences.stream()
                        .map(AnalyzedSentence::getEnText)
                        .collect(Collectors.joining("\n\n"));
                chapter.setTranslatedContent(fullEnText);
                chapterRepository.save(chapter);
                log.info("Đã cập nhật lại nội dung chương: {}", chapter.getTitle());
            }
        }
        log.info("Hoàn thành đồng bộ glossary ngầm cho story: {}", storyId);
    }
}
