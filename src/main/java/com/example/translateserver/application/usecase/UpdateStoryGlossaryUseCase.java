package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.domain.repository.StoryGlossaryRepository;
import com.example.translateserver.domain.util.StringSanitizer;
import com.example.translateserver.infrastructure.web.dto.UpdateGlossaryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateStoryGlossaryUseCase {
    private final StoryGlossaryRepository storyGlossaryRepository;
    private final PropagateGlossaryUseCase propagateGlossaryUseCase;

    public StoryGlossary execute(Long storyId, Long glossaryId, UpdateGlossaryRequest request) {
        StoryGlossary glossary = storyGlossaryRepository.findById(glossaryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy glossary với ID: " + glossaryId));

        if (!glossary.getStoryId().equals(storyId)) {
            throw new IllegalArgumentException("Bản ghi glossary này không thuộc về truyện có ID: " + storyId);
        }

        glossary.setViTerm(StringSanitizer.sanitize(request.getViTerm()));
        glossary.setEnTerm(StringSanitizer.sanitize(request.getEnTerm()));

        StoryGlossary saved = storyGlossaryRepository.save(glossary);
        propagateGlossaryUseCase.execute(storyId, saved);
        return saved;
    }
}
