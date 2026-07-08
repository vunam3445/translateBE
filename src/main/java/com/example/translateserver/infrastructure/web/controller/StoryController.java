package com.example.translateserver.infrastructure.web.controller;

import com.example.translateserver.application.usecase.*;
import com.example.translateserver.domain.model.Story;
import com.example.translateserver.infrastructure.web.dto.CreateStoryRequest;
import com.example.translateserver.infrastructure.web.dto.StoryResponse;
import com.example.translateserver.infrastructure.web.dto.UpdateStoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
public class StoryController {

    private final GetStoriesUseCase getStoriesUseCase;
    private final GetStoryUseCase getStoryUseCase;
    private final CreateStoryUseCase createStoryUseCase;
    private final UpdateStoryUseCase updateStoryUseCase;
    private final DeleteStoryUseCase deleteStoryUseCase;

    @GetMapping
    public ResponseEntity<List<StoryResponse>> getAllStories() {
        List<StoryResponse> responses = getStoriesUseCase.execute().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoryResponse> getStoryById(@PathVariable Long id) {
        return getStoryUseCase.execute(id)
                .map(story -> ResponseEntity.ok(mapToResponse(story)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoryResponse> createStory(@Valid @RequestBody CreateStoryRequest request) {
        Story story = createStoryUseCase.execute(request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(story));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoryResponse> updateStory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoryRequest request) {
        return updateStoryUseCase.execute(id, request.getTitle())
                .map(story -> ResponseEntity.ok(mapToResponse(story)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        if (deleteStoryUseCase.execute(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private StoryResponse mapToResponse(Story story) {
        if (story == null) {
            return null;
        }
        return StoryResponse.builder()
                .id(story.getId())
                .title(story.getTitle())
                .totalChapters(story.getTotalChapters())
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
}
