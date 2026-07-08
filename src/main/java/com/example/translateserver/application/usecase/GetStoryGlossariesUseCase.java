package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.domain.repository.StoryGlossaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetStoryGlossariesUseCase {
    private final StoryGlossaryRepository storyGlossaryRepository;

    public List<StoryGlossary> execute(Long storyId) {
        return storyGlossaryRepository.findByStoryId(storyId);
    }
}
