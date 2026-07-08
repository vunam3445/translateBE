package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.domain.repository.StoryGlossaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteStoryGlossaryUseCase {
    private final StoryGlossaryRepository storyGlossaryRepository;

    public boolean execute(Long storyId, Long glossaryId) {
        StoryGlossary glossary = storyGlossaryRepository.findById(glossaryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy glossary với ID: " + glossaryId));

        if (!glossary.getStoryId().equals(storyId)) {
            throw new IllegalArgumentException("Bản ghi glossary này không thuộc về truyện có ID: " + storyId);
        }

        storyGlossaryRepository.deleteById(glossaryId);
        return true;
    }
}
