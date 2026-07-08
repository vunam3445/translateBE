package com.example.translateserver.application.usecase;

import com.example.translateserver.domain.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteStoryUseCase {
    private final StoryRepository storyRepository;

    public boolean execute(Long id) {
        if (storyRepository.findById(id).isPresent()) {
            storyRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
