package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.AnalyzedSentence;
import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.model.ChapterStatus;
import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.domain.repository.AnalyzedSentenceRepository;
import com.example.translateserver.domain.repository.ChapterRepository;
import com.example.translateserver.domain.repository.StoryGlossaryRepository;
import com.example.translateserver.domain.service.SentenceAlignmentService;
import com.example.translateserver.domain.service.TextChunkingService;
import com.example.translateserver.domain.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslateChapterUseCase {

    private final ChapterRepository chapterRepository;
    private final StoryGlossaryRepository glossaryRepository;
    private final AnalyzedSentenceRepository analyzedSentenceRepository;
    private final TextChunkingService textChunkingService;
    private final SentenceAlignmentService sentenceAlignmentService;
    private final TranslationService translationService;

    // Bộ nhớ tạm theo dõi các chương đang được dịch thực tế trong JVM
    private final Set<Long> activeTranslations = ConcurrentHashMap.newKeySet();

    /**
     * Bắt đầu dịch chương truyện bất đồng bộ.
     *
     * @param storyId ID của câu chuyện
     * @param chapterId ID của chương cần dịch
     */
    public void executeAsync(Long storyId, Long chapterId) {
        if (activeTranslations.contains(chapterId)) {
            log.info("Chương {} đang trong tiến trình dịch ngầm, bỏ qua.", chapterId);
            return;
        }

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương với ID: " + chapterId));

        if (chapter.getStatus() == ChapterStatus.COMPLETED) {
            log.info("Chương {} đã được dịch hoàn thành, bỏ qua.", chapterId);
            return;
        }

        // Đánh dấu luồng dịch đang hoạt động
        activeTranslations.add(chapterId);

        // Đánh dấu trạng thái đang xử lý
        chapter.setStatus(ChapterStatus.PROCESSING);
        chapter.setTranslationProgress(5);
        chapterRepository.save(chapter);

        // Chạy bất đồng bộ
        CompletableFuture.runAsync(() -> {
            try {
                processTranslation(storyId, chapter);
            } catch (Exception e) {
                log.error("Lỗi nghiêm trọng trong tiến trình dịch ngầm chương {}", chapterId, e);
                chapter.setStatus(ChapterStatus.FAILED);
                chapter.setTranslationProgress(0);
                chapterRepository.save(chapter);
            } finally {
                // Giải phóng trạng thái luồng dịch
                activeTranslations.remove(chapterId);
            }
        });
    }

    private void processTranslation(Long storyId, Chapter chapter) throws Exception {
        log.info("Bắt đầu dịch ngầm chương: {} - {}", chapter.getId(), chapter.getTitle());

        // Bước 1: Đọc glossary từ DB
        List<StoryGlossary> glossaries = glossaryRepository.findByStoryId(storyId);
        StringBuilder glossaryPrompt = new StringBuilder();
        for (StoryGlossary g : glossaries) {
            glossaryPrompt.append("- ").append(g.getViTerm()).append(" -> ").append(g.getEnTerm()).append("\n");
        }
        
        chapter.setTranslationProgress(10);
        chapterRepository.save(chapter);

        // Bước 2: Chia nhỏ văn bản tiếng Việt
        String rawContent = chapter.getRawContent();
        List<String> chunks = textChunkingService.chunkText(rawContent);
        if (chunks.isEmpty()) {
            chapter.setStatus(ChapterStatus.COMPLETED);
            chapter.setTranslationProgress(100);
            chapter.setTranslatedContent("");
            chapterRepository.save(chapter);
            return;
        }

        log.info("Văn bản chương được chia thành {} chunk.", chunks.size());
        chapter.setTranslationProgress(15);
        chapterRepository.save(chapter);

        // Bước 3: Dịch từng chunk
        List<String> translatedChunks = new ArrayList<>();
        int index = 1;
        for (String chunk : chunks) {
            String translated = translationService.translateText(chunk, glossaryPrompt.toString());
            translatedChunks.add(translated);

            // Tiến trình dịch thô chiếm từ 15% -> 55%
            int progress = 15 + (int) (((double) index / chunks.size()) * 40);
            chapter.setTranslationProgress(progress);
            chapterRepository.save(chapter);
            index++;
        }

        // Hợp nhất nội dung tiếng Anh
        String fullTranslatedContent = String.join("\n\n", translatedChunks);
        chapter.setTranslatedContent(fullTranslatedContent);
        chapter.setTranslationProgress(60);
        chapterRepository.save(chapter);

        // Bước 4: Xóa các câu phân tích cũ của chương này nếu có để ghi đè
        analyzedSentenceRepository.deleteByChapterId(chapter.getId());

        // Bước 5: Gióng hàng câu (Sentence Alignment)
        List<SentenceAlignmentService.AlignedPair> alignedPairs = 
                sentenceAlignmentService.alignSentences(rawContent, fullTranslatedContent);

        log.info("Sau khi gióng hàng, tìm thấy {} cặp câu.", alignedPairs.size());
        chapter.setTranslationProgress(65);
        chapterRepository.save(chapter);

        if (alignedPairs.isEmpty()) {
            chapter.setStatus(ChapterStatus.COMPLETED);
            chapter.setTranslationProgress(100);
            chapterRepository.save(chapter);
            return;
        }

        // Bước 6: Không gọi Gemini để phân tích cú pháp (tránh rate limit), lưu các cặp câu trực tiếp vào DB
        List<AnalyzedSentence> sentencesToSave = new ArrayList<>();
        int pairIndex = 1;
        int totalPairs = alignedPairs.size();

        for (SentenceAlignmentService.AlignedPair pair : alignedPairs) {
            AnalyzedSentence sentence = AnalyzedSentence.builder()
                    .chapterId(chapter.getId())
                    .sentenceOrder(pairIndex)
                    .viText(pair.getViSentence())
                    .enText(pair.getEnSentence())
                    .tense("Unknown")
                    .wordsJsonMeta("")
                    .build();

            sentencesToSave.add(sentence);

            // Chỉ lưu DB khi tiến độ thay đổi thực tế và chia hết cho 5, hoặc là cặp câu cuối cùng
            int progress = 65 + (int) (((double) pairIndex / totalPairs) * 30);
            if (progress != chapter.getTranslationProgress() && (progress % 5 == 0 || pairIndex == totalPairs)) {
                chapter.setTranslationProgress(progress);
                chapterRepository.save(chapter);
            }
            
            pairIndex++;
        }

        // Lưu tất cả câu song ngữ
        analyzedSentenceRepository.saveAll(sentencesToSave);

        // Bước 7: Hoàn tất tiến trình
        chapter.setStatus(ChapterStatus.COMPLETED);
        chapter.setTranslationProgress(100);
        chapterRepository.save(chapter);
        log.info("Hoàn tất dịch ngầm chương {} thành công!", chapter.getId());
    }

    private String parseTenseFromJson(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
            if (root.has("tense")) {
                return root.get("tense").asText();
            }
        } catch (Exception e) {
            log.warn("Không thể parse tense từ JSON: {}", e.getMessage());
        }
        return "Unknown";
    }
}
