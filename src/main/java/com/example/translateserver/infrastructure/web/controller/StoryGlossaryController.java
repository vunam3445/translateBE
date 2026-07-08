package com.example.translateserver.infrastructure.web.controller;

import com.example.translateserver.application.usecase.*;
import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.infrastructure.web.dto.CreateGlossaryRequest;
import com.example.translateserver.infrastructure.web.dto.GlossaryResponse;
import com.example.translateserver.infrastructure.web.dto.UpdateGlossaryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stories/{storyId}/glossaries")
@RequiredArgsConstructor
public class StoryGlossaryController {

    private final GetStoryGlossariesUseCase getStoryGlossariesUseCase;
    private final CreateStoryGlossaryUseCase createStoryGlossaryUseCase;
    private final UpdateStoryGlossaryUseCase updateStoryGlossaryUseCase;
    private final DeleteStoryGlossaryUseCase deleteStoryGlossaryUseCase;

    @GetMapping
    public ResponseEntity<List<GlossaryResponse>> getGlossaries(@PathVariable Long storyId) {
        List<GlossaryResponse> responses = getStoryGlossariesUseCase.execute(storyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<GlossaryResponse> createGlossary(
            @PathVariable Long storyId,
            @Valid @RequestBody CreateGlossaryRequest request) {
        StoryGlossary glossary = createStoryGlossaryUseCase.execute(storyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(glossary));
    }

    @PutMapping("/{glossaryId}")
    public ResponseEntity<GlossaryResponse> updateGlossary(
            @PathVariable Long storyId,
            @PathVariable Long glossaryId,
            @Valid @RequestBody UpdateGlossaryRequest request) {
        StoryGlossary glossary = updateStoryGlossaryUseCase.execute(storyId, glossaryId, request);
        return ResponseEntity.ok(mapToResponse(glossary));
    }

    @DeleteMapping("/{glossaryId}")
    public ResponseEntity<Void> deleteGlossary(
            @PathVariable Long storyId,
            @PathVariable Long glossaryId) {
        if (deleteStoryGlossaryUseCase.execute(storyId, glossaryId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private GlossaryResponse mapToResponse(StoryGlossary glossary) {
        if (glossary == null) {
            return null;
        }
        return GlossaryResponse.builder()
                .id(glossary.getId())
                .storyId(glossary.getStoryId())
                .viTerm(glossary.getViTerm())
                .enTerm(glossary.getEnTerm())
                .isManualAddition(glossary.isManualAddition())
                .createdAt(glossary.getCreatedAt())
                .build();
    }
}
