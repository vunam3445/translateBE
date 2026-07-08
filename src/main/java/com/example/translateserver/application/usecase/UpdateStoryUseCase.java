package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.StoryRepository;
import com.example.translateserver.domain.util.StringSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateStoryUseCase {
    private final StoryRepository storyRepository;

    public Optional<Story> execute(Long id, String title) {
        return storyRepository.findById(id).map(existingStory -> {
            existingStory.setTitle(StringSanitizer.sanitize(title));
            return storyRepository.save(existingStory);
        });
    }
}

