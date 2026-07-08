package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.domain.repository.StoryRepository;
import com.example.translateserver.domain.repository.StoryGlossaryRepository;
import com.example.translateserver.domain.util.StringSanitizer;
import com.example.translateserver.infrastructure.web.dto.CreateGlossaryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateStoryGlossaryUseCase {
    private final StoryRepository storyRepository;
    private final StoryGlossaryRepository storyGlossaryRepository;
    private final PropagateGlossaryUseCase propagateGlossaryUseCase;

    public StoryGlossary execute(Long storyId, CreateGlossaryRequest request) {
        storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy truyện với ID: " + storyId));

        String sanitizedViTerm = StringSanitizer.sanitize(request.getViTerm());
        String sanitizedEnTerm = StringSanitizer.sanitize(request.getEnTerm());

        java.util.Optional<StoryGlossary> existingOpt = storyGlossaryRepository.findByStoryIdAndViTerm(storyId, sanitizedViTerm);
        StoryGlossary saved;
        if (existingOpt.isPresent()) {
            StoryGlossary existing = existingOpt.get();
            existing.setEnTerm(sanitizedEnTerm);
            saved = storyGlossaryRepository.save(existing);
        } else {
            StoryGlossary glossary = StoryGlossary.builder()
                    .storyId(storyId)
                    .viTerm(sanitizedViTerm)
                    .enTerm(sanitizedEnTerm)
                    .description(null)
                    .isManualAddition(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            saved = storyGlossaryRepository.save(glossary);
        }

        propagateGlossaryUseCase.execute(storyId, saved);
        return saved;
    }
}
