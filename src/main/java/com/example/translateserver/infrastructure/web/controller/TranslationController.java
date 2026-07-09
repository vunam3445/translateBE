package com.example.translateserver.infrastructure.web.controller;

import com.example.translateserver.application.usecase.GetSentencesUseCase;
import com.example.translateserver.application.usecase.TranslateChapterUseCase;
import com.example.translateserver.application.usecase.TranslatePhraseUseCase;
import com.example.translateserver.application.usecase.GetGlossaryMismatchesUseCase;
import com.example.translateserver.application.usecase.UpdateSentenceUseCase;
import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.domain.repository.ChapterRepository;
import com.example.translateserver.domain.service.SentenceTenseAnalyzer;
import com.example.translateserver.infrastructure.web.dto.AnalyzeTenseRequest;
import com.example.translateserver.infrastructure.web.dto.PhraseTranslateRequest;
import com.example.translateserver.infrastructure.web.dto.PhraseTranslationResponse;
import com.example.translateserver.infrastructure.web.dto.SentenceResponse;
import com.example.translateserver.infrastructure.web.dto.TranslationStatusResponse;
import com.example.translateserver.infrastructure.web.dto.GlossaryMismatchResponse;
import com.example.translateserver.infrastructure.web.dto.UpdateSentenceRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TranslationController {

    private final TranslateChapterUseCase translateChapterUseCase;
    private final GetSentencesUseCase getSentencesUseCase;
    private final TranslatePhraseUseCase translatePhraseUseCase;
    private final GetGlossaryMismatchesUseCase getGlossaryMismatchesUseCase;
    private final UpdateSentenceUseCase updateSentenceUseCase;
    private final ChapterRepository chapterRepository;
    private final SentenceTenseAnalyzer sentenceTenseAnalyzer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/stories/{storyId}/chapters/{chapterId}/translate")
    public ResponseEntity<Void> translateChapter(
            @PathVariable Long storyId,
            @PathVariable Long chapterId) {
        
        log.info("Nhận yêu cầu dịch chương {} của truyện {}", chapterId, storyId);
        translateChapterUseCase.executeAsync(storyId, chapterId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/stories/{storyId}/chapters/{chapterId}/status")
    public ResponseEntity<TranslationStatusResponse> getTranslationStatus(
            @PathVariable Long storyId,
            @PathVariable Long chapterId) {

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương với ID: " + chapterId));

        TranslationStatusResponse response = TranslationStatusResponse.builder()
                .chapterId(chapter.getId())
                .status(chapter.getStatus().name())
                .progress(chapter.getTranslationProgress())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stories/{storyId}/chapters/{chapterId}/sentences")
    public ResponseEntity<List<SentenceResponse>> getSentences(
            @PathVariable Long storyId,
            @PathVariable Long chapterId) {

        List<SentenceResponse> responses = getSentencesUseCase.execute(chapterId).stream()
                .map(sentence -> SentenceResponse.builder()
                        .id(sentence.getId())
                        .chapterId(sentence.getChapterId())
                        .sentenceOrder(sentence.getSentenceOrder())
                        .viText(sentence.getViText())
                        .enText(sentence.getEnText())
                        .tense(sentence.getTense())
                        .wordsJsonMeta(sentence.getWordsJsonMeta())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/translate/phrase")
    public ResponseEntity<PhraseTranslationResponse> translatePhrase(
            @Valid @RequestBody PhraseTranslateRequest request) {

        log.info("Yêu cầu dịch cụm từ: '{}'", request.getPhrase());
        String rawJson = translatePhraseUseCase.execute(request.getPhrase(), request.getContext());
        
        // Parse JSON thô từ Gemini thành DTO trả về cho client
        String meaning = request.getPhrase();
        String example = "";
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            if (root.has("m")) {
                meaning = root.get("m").asText();
            } else if (root.has("mean")) {
                meaning = root.get("mean").asText();
            } else if (root.has("meaning")) {
                meaning = root.get("meaning").asText();
            }
            
            if (root.has("ex")) {
                example = root.get("ex").asText();
            } else if (root.has("example")) {
                example = root.get("example").asText();
            }
        } catch (Exception e) {
            log.warn("Không thể phân tích JSON trả về từ Gemini: {}. Lỗi: {}", rawJson, e.getMessage());
            meaning = rawJson; // Nếu lỗi thì trả về chuỗi thô luôn
        }

        PhraseTranslationResponse response = PhraseTranslationResponse.builder()
                .meaning(meaning)
                .example(example)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stories/{storyId}/chapters/{chapterId}/glossary-mismatches")
    public ResponseEntity<List<GlossaryMismatchResponse>> getGlossaryMismatches(
            @PathVariable Long storyId,
            @PathVariable Long chapterId) {
        return ResponseEntity.ok(getGlossaryMismatchesUseCase.execute(storyId, chapterId));
    }

    @PutMapping("/stories/{storyId}/chapters/{chapterId}/sentences/{sentenceId}")
    public ResponseEntity<Void> updateSentence(
            @PathVariable Long storyId,
            @PathVariable Long chapterId,
            @PathVariable Long sentenceId,
            @RequestBody @Valid UpdateSentenceRequest request) {
        updateSentenceUseCase.execute(chapterId, sentenceId, request.getEnText());
        return ResponseEntity.ok().build();
    }

    /**
     * Phân tích thì (Tense) của một câu tiếng Anh đơn lẻ sử dụng Apache OpenNLP (offline).
     * Trả về JSON đầy đủ: tên thì (EN/VI), cấu trúc, dấu hiệu nhận biết, giải thích lý do, danh sách từ vựng.
     *
     * POST /api/v1/translate/analyze-tense
     * Body: { "sentence": "She has been working here for years." }
     */
    @PostMapping("/translate/analyze-tense")
    public ResponseEntity<String> analyzeTense(
            @Valid @RequestBody AnalyzeTenseRequest request) {

        log.info("Yêu cầu phân tích thì cho câu: '{}'", request.getSentence());
        String rawJson = sentenceTenseAnalyzer.analyzeSentence(request.getSentence());
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(rawJson);
    }
}
