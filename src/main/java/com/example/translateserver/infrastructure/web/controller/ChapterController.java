package com.example.translateserver.infrastructure.web.controller;

import com.example.translateserver.application.usecase.GetChaptersUseCase;
import com.example.translateserver.application.usecase.UploadChapterUseCase;
import com.example.translateserver.application.usecase.DeleteChapterUseCase;
import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.infrastructure.web.dto.ChapterResponse;
import com.example.translateserver.infrastructure.web.dto.UploadChapterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stories/{storyId}/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final UploadChapterUseCase uploadChapterUseCase;
    private final GetChaptersUseCase getChaptersUseCase;
    private final DeleteChapterUseCase deleteChapterUseCase;

    @PostMapping
    public ResponseEntity<ChapterResponse> uploadChapter(
            @PathVariable Long storyId,
            @Valid @RequestBody UploadChapterRequest request) {
        
        Chapter chapter = uploadChapterUseCase.execute(storyId, request.getTitle(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(chapter));
    }

    @GetMapping
    public ResponseEntity<List<ChapterResponse>> getChapters(@PathVariable Long storyId) {
        List<ChapterResponse> responses = getChaptersUseCase.execute(storyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{chapterId}")
    public ResponseEntity<Void> deleteChapter(
            @PathVariable Long storyId,
            @PathVariable Long chapterId) {
        
        try {
            boolean deleted = deleteChapterUseCase.execute(storyId, chapterId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ChapterResponse mapToResponse(Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        return ChapterResponse.builder()
                .id(chapter.getId())
                .storyId(chapter.getStoryId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .status(chapter.getStatus().name())
                .translatedContent(chapter.getTranslatedContent())
                .translationProgress(chapter.getTranslationProgress())
                .createdAt(chapter.getCreatedAt())
                .build();
    }
}
