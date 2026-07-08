package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.StoryRepository;
import com.example.translateserver.domain.util.StringSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStoryUseCase {
    private final StoryRepository storyRepository;

    public Story execute(String title) {
        Story story = Story.builder()
                .title(StringSanitizer.sanitize(title))
                .totalChapters(0)
                .build();
        return storyRepository.save(story);
    }
}

