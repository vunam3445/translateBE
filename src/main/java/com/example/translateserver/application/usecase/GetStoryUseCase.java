package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.model.Story;
import com.example.translateserver.domain.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetStoryUseCase {
    private final StoryRepository storyRepository;

    public Optional<Story> execute(Long id) {
        return storyRepository.findById(id);
    }
}
